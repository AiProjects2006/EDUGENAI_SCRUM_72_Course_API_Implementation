package karate.course;

import com.intuit.karate.junit5.Karate;

class CourseApiRunner {
    @Karate.Test
    Karate testCourseApi() {
        return Karate.run(
                        "course-crud",
                        "course-validation",
                        "subject-crud",
                        "subject-validation",
                        "module-crud",
                        "module-validation")
                .relativeTo(getClass());
    }
}
