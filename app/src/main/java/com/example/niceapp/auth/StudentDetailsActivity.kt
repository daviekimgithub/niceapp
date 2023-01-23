package com.example.niceapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.example.niceapp.R
import com.example.niceapp.general.HomeActivity
import com.example.niceapp.modals.StudentData
import com.example.niceapp.utils.AppPreferences
import kotlinx.android.synthetic.main.activity_student_details.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class StudentDetailsActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    private lateinit var collegeID: String
    private lateinit var deptID: String
    private lateinit var yearID: String
    private lateinit var sectionID: String

    private var CAMPUS_NAME = Pair("University of Eldoret, School of Science", "UOE")

    private var BIOSCI_DEPT = Pair("Department of Biological Sciences", "BIOSCI")
    private var CHEMBIO_DEPT = Pair("Department of Chemistry and Biochemistry", "CHEMBIO")
    private var MATCOMP_DEPT = Pair("Department of Mathematics and Computer Science", "MATCOMP")
    private var PHYC_DEPT = Pair("Department of Physics", "PHYC")

//    private var IT_DEPT = Pair("Information Technology", "IT")
//    private var ECE_DEPT = Pair("Electronics and Communication Engineering", "ECE")
//    private var EEE_DEPT = Pair("Electrical and Electronics Engineering","EEE")
//    private var CIV_DEPT = Pair("Civil Engineering","CIV")
//    private var MECH_DEPT = Pair("Mechanical Engineering","MECH")
//    private var CHEM_DEPT = Pair("Chemical Engineering","CHEM")

    private var FIRST_YEAR = Pair("1st Year", "1")
    private var SECOND_YEAR = Pair("2nd Year", "2")
    private var THIRD_YEAR = Pair("3rd Year", "3")
    private var FOURTH_YEAR = Pair("4th Year", "4")

    private var A_SECTION = Pair("1 SEMESTER", "A")
    private var B_SECTION = Pair("2 SEMESTER", "B")
    private var C_SECTION = Pair("ATTACHMENT", "C")

    private val TAG = "TOKENS_DATA"

    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=" + AppPreferences.AUTH_KEY_FCM
    private val contentType = "application/json"
    var TOPIC: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_details)
        setUpCollegeList()
        setUpDepartmentList()
        setUpSectionList()
        setUpYearList()

        database = Firebase.database.reference

        AppPreferences.init(this)

        btnSubmit.setOnClickListener {
            val studentName = editName.text.toString().trim()
            val studentPhoneNumber = editPhone.text.toString().trim()
            val collegeName = collegeList.text.toString().trim()
            val graduationYear = yearsList.text.toString().trim()
            val studentDept = deptSpinner.text.toString().trim()
            val studentSection = sectionSpinner.text.toString().trim()

            //TODO: Phone Number (Only 10 Digits length!=10) and Name (only Characters allowed) Validation
            if(isNullOrEmpty(studentName)) {
                editName.error = "Please enter Name"
            } else if(!isValidName(studentName.toString())) {
                editName.error = null
                editName.error = "Name should be only Alphabets"
            }else if(isNullOrEmpty(studentPhoneNumber)) {
                editName.error = null
                editPhone.error = "Please enter Phone Number"
            } else if(editPhone.length() !=10) {
                editPhone.error = null
                editPhone.error = "Please enter Valid Phone Number "
            } else if(isNullOrEmpty(collegeName)) {
                editPhone.error = null
                collegeList.error = "Please choose College Name"
            } else if(isNullOrEmpty(graduationYear)) {
                collegeList.error = null
                yearsList.error = "Please choose current Studying Year"
            } else if(isNullOrEmpty(studentDept)) {
                yearsList.error = null
                deptSpinner.error = "Please choose Department"
            } else if(isNullOrEmpty(studentSection)) {
                deptSpinner.error = null
                sectionSpinner.error = "Please choose Section"
            } else {
                sectionSpinner.error = null
                btnSubmit.isEnabled = false

                val intent=intent
                val email=intent.getStringExtra("Email")
                val provider = intent.getStringExtra("provider")

                val collegeID: String
                val deptID: String
                val yearID: String
                val sectionID: String

                if(studentDept.toString().equals("Department of Biological Sciences")) {
                    deptID = "BIO"
                } else if(studentDept.toString().equals("Department of Chemistry and Biochemistry")) {
                    deptID = "CHEM"
                } else if(studentDept.toString().equals("Department of Mathematics and Computer Science")) {
                    deptID = "COMP"
                }  else if(studentDept.toString().equals("Department of Physics")) {
                    deptID = "PHY"
                }
                else {
                    deptID = MATCOMP_DEPT.second

                }

                if(graduationYear.toString().equals("1st Year")){
                    yearID = FIRST_YEAR.second
                }
                else if(graduationYear.toString().equals("2nd Year")){
                    yearID = SECOND_YEAR.second
                }
                else if(graduationYear.toString().equals("3rd Year")){
                    yearID = THIRD_YEAR.second
                }
                else {
                    yearID = FOURTH_YEAR.second
                }

                if(studentSection.toString().equals("A Section")){
                    sectionID = A_SECTION.second
                }
                else if(studentSection.toString().equals("B Section")){
                    sectionID = B_SECTION.second
                }
                else {
                    sectionID = C_SECTION.second
                }

                var userID = "UOE"+"_"+deptID + "_"+ yearID+"_"+sectionID
                //showToast(userID + " " + email)


                //TODO: Should send Email ID from Authentication Activity and pass Parameter to Database
                writeNewUser(
                    userID,
                    studentName,
                    email,
                    studentPhoneNumber,
                    collegeName,
                    graduationYear,
                    studentDept,
                    studentSection,
                    provider
                )

            }
        }
    }

    private fun writeNewUser(
        userId: String,
        name: String,
        email: String?,
        phone: String,
        collegeName: String,
        graduationYear: String,
        studentDept: String,
        studentSection: String,
        provider: String?
    ) {
        val user = StudentData(userId, name.toString(), email.toString(), phone.toString(), collegeName.toString(), graduationYear.toString(), studentDept.toString(), studentSection.toString(), provider.toString())
        database.child("students_data").push().setValue(user)

        AppPreferences.isLogin = true
        AppPreferences.studentName = name.toString()
        AppPreferences.studentID = userId.toString()
        AppPreferences.studentEmailID = email.toString()

        // subscribing the student to his class topic
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + AppPreferences.studentID)

        showToast("Details Submitted Successfully !")

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()


    }

    private fun isNullOrEmpty(str: String): Boolean {
        if (str != null && !str.trim().isEmpty())
            return false
        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setUpCollegeList() {
        val collegeNames = listOf(CAMPUS_NAME.first)
        val adapter = ArrayAdapter(
            this,
            R.layout.list_item, collegeNames
        )
        (collegeList as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setUpYearList() {
        val yearNumbers = listOf(
            FIRST_YEAR.first,
            SECOND_YEAR.first,
            THIRD_YEAR.first,
            FOURTH_YEAR.first
        )
        val adapter = ArrayAdapter(
            this,
            R.layout.list_item, yearNumbers
        )
        (yearsList as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setUpDepartmentList() {
        val deptNames = listOf(BIOSCI_DEPT.first, CHEMBIO_DEPT.first,MATCOMP_DEPT.first,PHYC_DEPT.first)
        val adapter = ArrayAdapter(
            this,
            R.layout.list_item, deptNames
        )
        (deptSpinner as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setUpSectionList() {
        val sectionNames = listOf(A_SECTION.first, B_SECTION.first, C_SECTION.first)
        val adapter = ArrayAdapter(
            this,
            R.layout.list_item, sectionNames
        )
        (sectionSpinner as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun isValidName(name: String): Boolean {
        val NAME_PATTERN = ("^[A-Z a-z]+$")
        val pattern: Pattern = Pattern.compile(NAME_PATTERN)
        val matcher: Matcher = pattern.matcher(name)
        return matcher.matches()
    }

    private infix fun Boolean.and(length: Int): Boolean {
        return length != 10
    }


}