Feature: Course API CRUD and retrieval

  Background:
    * def baseUrl = karate.properties['baseUrl'] || 'http://localhost:8082'
    * url baseUrl
    * header Accept = 'application/json'
    * header Content-Type = 'application/json'

  Scenario: Create, retrieve, filter, update, and delete a course
    * def subjectName = 'Karate subject ' + java.util.UUID.randomUUID()
    Given path 'api/v1/subjects'
    And request { name: '#(subjectName)', description: 'Created by the Karate API suite' }
    When method post
    Then status 201
    And match response.success == true
    And match response.message == 'Subject created successfully.'
    And match response.data contains { subjectId: '#number', name: '#(subjectName)' }
    * def subjectId = response.data.subjectId

    * def creatorId = java.lang.System.currentTimeMillis()
    * def courseTitle = 'Karate course ' + java.util.UUID.randomUUID()
    Given path 'api/v1/courses'
    And request
      """
      {
        "subjectId": "#(subjectId)",
        "creatorId": "#(creatorId)",
        "title": "#(courseTitle)",
        "description": "Created by the Karate API suite",
        "thumbnailUrl": "https://example.test/karate-course.png",
        "level": "BEGINNER",
        "status": "DRAFT"
      }
      """
    When method post
    Then status 201
    And match response.success == true
    And match response.message == 'Course created successfully.'
    And match response.data contains
      """
      {
        courseId: '#number',
        subjectId: '#(subjectId)',
        creatorId: '#(creatorId)',
        title: '#(courseTitle)',
        description: 'Created by the Karate API suite',
        thumbnailUrl: 'https://example.test/karate-course.png',
        level: 'BEGINNER',
        status: 'DRAFT'
      }
      """
    * def courseId = response.data.courseId

    Given path 'api/v1/courses'
    When method get
    Then status 200
    And match response.success == true
    And match response.message == 'Courses retrieved successfully.'
    And match response.data[*].courseId contains courseId

    Given path 'api/v1/courses', courseId
    When method get
    Then status 200
    And match response.success == true
    And match response.message == 'Course retrieved successfully.'
    And match response.data.courseId == courseId
    And match response.data.subjectId == subjectId
    And match response.data.creatorId == creatorId
    And match response.data.title == courseTitle

    Given path 'api/v1/courses/subject', subjectId
    When method get
    Then status 200
    And match response.success == true
    And match response.message == 'Courses retrieved successfully.'
    And match response.data[*].courseId contains courseId
    And match each response.data contains { subjectId: '#(subjectId)' }

    Given path 'api/v1/courses/creator', creatorId
    When method get
    Then status 200
    And match response.success == true
    And match response.message == 'Courses retrieved successfully.'
    And match response.data[*].courseId contains courseId
    And match each response.data contains { creatorId: '#(creatorId)' }

    * def updatedTitle = 'Updated Karate course ' + java.util.UUID.randomUUID()
    Given path 'api/v1/courses', courseId
    And request { title: '#(updatedTitle)', status: 'DRAFT' }
    When method put
    Then status 400
    And match response.success == false
    And match response.message == '#string'
    And match response.data == null

    Given path 'api/v1/courses', courseId
    And request { title: '#(updatedTitle)', level: 'INTERMEDIATE' }
    When method put
    Then status 400
    And match response.success == false
    And match response.message == '#string'
    And match response.data == null

    Given path 'api/v1/courses', courseId
    And request
      """
      {
        "title": "#(updatedTitle)",
        "description": "Updated by the Karate API suite",
        "thumbnailUrl": "https://example.test/updated-karate-course.png",
        "level": "INTERMEDIATE",
        "status": "PUBLISHED"
      }
      """
    When method put
    Then status 200
    And match response.success == true
    And match response.message == 'Course updated successfully.'
    And match response.data contains
      """
      {
        courseId: '#(courseId)',
        subjectId: '#(subjectId)',
        creatorId: '#(creatorId)',
        title: '#(updatedTitle)',
        description: 'Updated by the Karate API suite',
        thumbnailUrl: 'https://example.test/updated-karate-course.png',
        level: 'INTERMEDIATE',
        status: 'PUBLISHED'
      }
      """

    Given path 'api/v1/courses', courseId
    When method delete
    Then status 200
    And match response == { success: true, message: 'Course deleted successfully.', data: null }

    Given path 'api/v1/courses', courseId
    When method get
    Then status 404
    And match response == { success: false, message: 'Course not found', data: null }

    Given path 'api/v1/courses', courseId
    And request
      """
      {
        "title": "Updated after deletion",
        "level": "BEGINNER",
        "status": "DRAFT"
      }
      """
    When method put
    Then status 404
    And match response == { success: false, message: 'Course not found', data: null }

    Given path 'api/v1/courses', courseId
    When method delete
    Then status 404
    And match response == { success: false, message: 'Course not found', data: null }

    Given path 'api/v1/subjects', subjectId
    When method delete
    Then status 200
