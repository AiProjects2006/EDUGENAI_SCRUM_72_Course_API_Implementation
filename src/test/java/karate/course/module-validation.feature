Feature: Module request validation

  Background:
    * def baseUrl = karate.properties['baseUrl'] || 'http://localhost:8082'
    * url baseUrl
    * header Accept = 'application/json'
    * header Content-Type = 'application/json'

  Scenario: Reject a module create request with the title missing
    Given path 'api/v1/modules'
    And request {}
    When method post
    Then status 400
    And match response.success == false
    And match response.message == '#string'
    And match response.data == null

  Scenario: Reject a module create request with a blank title
    Given path 'api/v1/modules'
    And request { title: ' ', courseId: '#(java.lang.System.nanoTime())' }
    When method post
    Then status 400
    And match response == { success: false, message: 'Module title is required', data: null }

  Scenario: Reject a module create request with the course ID missing
    Given path 'api/v1/modules'
    And request { title: 'Valid title' }
    When method post
    Then status 400
    And match response == { success: false, message: 'Course ID is required', data: null }

  Scenario: Return not found when creating a module for a deleted course
    * def subjectName = 'Deleted module parent subject ' + java.util.UUID.randomUUID()
    Given path 'api/v1/subjects'
    And request { name: '#(subjectName)' }
    When method post
    Then status 201
    * def subjectId = response.data.subjectId

    Given path 'api/v1/courses'
    And request
      """
      {
        "subjectId": "#(subjectId)",
        "creatorId": "#(java.lang.System.nanoTime())",
        "title": "Deleted module parent course",
        "level": "BEGINNER",
        "status": "DRAFT"
      }
      """
    When method post
    Then status 201
    * def courseId = response.data.courseId

    Given path 'api/v1/courses', courseId
    When method delete
    Then status 200

    Given path 'api/v1/subjects', subjectId
    When method delete
    Then status 200

    Given path 'api/v1/modules'
    And request { title: 'Module with missing course', courseId: '#(courseId)' }
    When method post
    Then status 404
    And match response.success == false
    And match response.message == 'Course not found with id : ' + courseId
    And match response.data == null
