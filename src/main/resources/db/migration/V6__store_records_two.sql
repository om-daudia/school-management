--standard records
INSERT INTO standard (standard, school_id) VALUES (1, 3);

--division records
INSERT INTO division (division, standard_id) VALUES ('A', 6);
INSERT INTO division (division, standard_id) VALUES ('B', 6);


--student records

--man
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('Mora Thomos', 263, 87.66, 'Pass', 6);
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('Aman Gupta', 223, 74.33, 'Pass', 6);
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('Bala Brown', 180, 60.0, 'Fail', 7);
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('Ramo Lee', 270, 90.0, 'Pass', 7);
INSERT INTO student (student_name, obtain_marks, percentage, result, division_id) VALUES ('Ren Green', 0, 0, 'Fail', 7);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Java', 90, 11);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Python', 85, 11);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('CLanguage', 88, 11);
-- test change

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Java', 70, 12);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Python', 75, 12);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('CLanguage', 78, 12);

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Math', 60, 13);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Science', 55, 13);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('English', 65, 13);

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Math', 95, 14);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Science', 90, 14);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('English', 85, 14);

INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Math', 17, 15);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('Science', 48, 15);
INSERT INTO subjectmark (subject_name, marks, student_id) VALUES ('English', 82, 15);
