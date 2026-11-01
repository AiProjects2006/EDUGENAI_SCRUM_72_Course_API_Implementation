Feature: Module API CRUD

  Background:
    * def baseUrl = karate.properties['baseUrl'] || 'http://localhost:8082'
    * url baseUrl
    * header Accept = 'application/json'
    * header Content-Type = 'application/json'

  Scenario: Create, retrieve, update, and delete a module
    * def subjectName = 'Karate module subject ' + java.util.UUID.randomUUID()
    Given path 'api/v1/subjects'
    And request { name: '#(subjectName)', description: 'Created by the Karate API suite' }
    When method post
    Then status 201
    * def subjectId = response.data.subjectId

    * def creatorId = java.lang.System.currentTimeMillis()
    * def courseTitle = 'Karate module course ' + java.util.UUID.randomUUID()
    Given path 'api/v1/courses'
    And request
      """
      {
        "subjectId": "#(subjectId)",
        "creatorId": "#(creatorId)",
        "title": "#(courseTitle)",
        "level": "BEGINNER",
        "status": "DRAFT"
      }
      """
    When method post
    Then status 201
    * def courseId = response.data.courseId

    * def moduleTitle = 'Karate module ' + java.util.UUID.randomUUID()
    Given path 'api/v1/modules'
    And request { title: '#(moduleTitle)', description: 'Created by the Karate API suite', courseId: '#(courseId)' }
    When method post
    Then status 201
    And match response.success == true
    And match response.data contains
      """
      {
        moduleId: '#number',
        title: '#(moduleTitle)',
        description: 'Created by the Karate API suite',
        courseId: '#(courseId)'
      }
      """
    * def moduleId = response.data.moduleId

    Given path 'api/v1/modules'
    When method get
    Then status 200
    And match response.success == true
    And match response.data[*].moduleId contains moduleId

    Given path 'api/v1/modules', moduleId
    When method get
    Then status 200
    And match response.data.moduleId == moduleId
    And match response.data.courseId == courseId

    Given path 'api/v1/modules', moduleId
    And request { title: ' ', description: 'Invalid update' }
    When method put
    Then status 400
    And match response == { success: false, message: 'Module title is required', data: null }

    Given path 'api/v1/modules', moduleId
    When method get
    Then status 200
    And match response.data.title == moduleTitle

    * def updatedTitle = 'Updated Karate module ' + java.util.UUID.randomUUID()
    Given path 'api/v1/modules', moduleId
    And request { title: '#(updatedTitle)', description: 'Updated by the Karate API suite' }
    When method put
    Then status 200
    And match response.success == true
    And match response.data contains
      """
      {
        moduleId: '#(moduleId)',
        title: '#(updatedTitle)',
        description: 'Updated by the Karate API suite',
        courseId: '#(courseId)'
      }
      """

    Given path 'api/v1/modules', moduleId
    When method delete
    Then status 200
    And match response == { success: true, message: 'Module deleted successfully.', data: null }

    Given path 'api/v1/modules', moduleId
    When method get
    Then status 404
    And match response.success == false
    And match response.message == 'Module not found with id : ' + moduleId
    And match response.data == null

    Given path 'api/v1/modules', moduleId
    And request { title: 'Updated after deletion' }
    When method put
    Then status 404
    And match response == { success: false, message: 'Module not found with id : ' + moduleId, data: null }

    Given path 'api/v1/modules', moduleId
    When method delete
    Then status 404
    And match response == { success: false, message: 'Module not found with id : ' + moduleId, data: null }

    Given path 'api/v1/courses', courseId
    When method delete
    Then status 200

    Given path 'api/v1/subjects', subjectId
    When method delete
    Then status 200
