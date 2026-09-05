create table subjectmark(
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    subject_name VARCHAR(50) NOT NULL,
    marks DOUBLE PRECISION NOT NULL,
    student_id INT,
    CONSTRAINT fk_student
        FOREIGN KEY (student_id)
        REFERENCES student(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
