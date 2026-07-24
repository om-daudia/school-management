--student table
create table student(
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL,
    obtain_marks FLOAT,
    percentage FLOAT,
    result VARCHAR(10),
    division_id INT,
    CONSTRAINT fk_division
        FOREIGN KEY (division_id)
        REFERENCES division(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);