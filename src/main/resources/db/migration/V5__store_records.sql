
--school records
INSERT INTO school (school_name) VALUES ('Jumbo School');
INSERT INTO school (school_name) VALUES ('Rainbow Academy');
INSERT INTO school (school_name) VALUES ('Green School');

--standard records
INSERT INTO standard (standard, school_id) VALUES (1, 1);
INSERT INTO standard (standard, school_id) VALUES (2, 1);
INSERT INTO standard (standard, school_id) VALUES (3, 2);
INSERT INTO standard (standard, school_id) VALUES (4, 2);
INSERT INTO standard (standard, school_id) VALUES (5, 2);

--division records
INSERT INTO division (division, standard_id) VALUES ('A', 1);
INSERT INTO division (division, standard_id) VALUES ('B', 1);
INSERT INTO division (division, standard_id) VALUES ('C', 1);
INSERT INTO division (division, standard_id) VALUES ('A', 2);
INSERT INTO division (division, standard_id) VALUES ('B', 2);

--student records
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('Alice Johnson', 263, 87.66, 'Pass', 1);
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('Bob Smith', 223, 74.33, 'Pass', 1);
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('Charlie Brown', 180, 60.0, 'Fail', 2);
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('David Lee', 270, 90.0, 'Pass', 1);
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('Eva Green', 0, 0, 'Fail', 1);
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('Fiona White', 0, 0, 'Fail', 2);
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('George Black', 0, 0, 'Fail', 2);
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('Hannah Adams', 165, 55.0, 'Pass', 2);
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('Ian Wright', 300, 60.0, 'Pass', 1);
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('Jane Doe', 290, 96.66, 'Pass', 1);

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Math', 90, 1);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Science', 85, 1);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('English', 88, 1);

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Math', 70, 2);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Science', 75, 2);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('English', 78, 2);

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Math', 60, 3);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Science', 55, 3);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('English', 65, 3);

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Math', 95, 4);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Science', 90, 4);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('English', 85, 4);

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Math', 17, 5);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Science', 78, 5);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('English', 82, 5);

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Math', 50, 6);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Science', 60, 6);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('English', 55, 6);

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Math', 75, 7);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Science', 70, 7);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('English', 68, 7);

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Math', 88, 8);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Science', 85, 8);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('English', 90, 8);

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Math', 60, 9);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Science', 58, 9);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('English', 62, 9);

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Math', 98, 10);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Science', 95, 10);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('English', 97, 10);
