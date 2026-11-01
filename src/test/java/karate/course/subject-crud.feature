Feature: Subject API CRUD

  Background:
    * def baseUrl = karate.properties['baseUrl'] || 'http://localhost:8082'
    * url baseUrl
    * header Accept = 'application/json'
    * header Content-Type = 'application/json'

  Scenario: Create, retrieve, update, and delete a subject
    * def subjectName = 'Karate subject ' + java.util.UUID.randomUUID()
    Given path 'api/v1/subjects'
    And request { name: '#(subjectName)', description: 'Created by the Karate API suite' }
    When method post
    Then status 201
    And match response.success == true
    And match response.message == 'Subject created successfully.'
    And match response.data contains
      """
      {
        subjectId: '#number',
        name: '#(subjectName)',
        description: 'Created by the Karate API suite'
      }
      """
    * def subjectId = response.data.subjectId

    Given path 'api/v1/subjects'
    When method get
    Then status 200
    And match response.success == true
    And match response.message == 'Subjects retrieved successfully.'
    And match response.data[*].subjectId contains subjectId

    Given path 'api/v1/subjects', subjectId
    When method get
    Then status 200
    And match response.success == true
    And match response.message == 'Subject retrieved successfully.'
    And match response.data.subjectId == subjectId
    And match response.data.name == subjectName

    * def updatedName = 'Updated Karate subject ' + java.util.UUID.randomUUID()
    Given path 'api/v1/subjects', subjectId
    And request { name: '#(updatedName)', description: 'Updated by the Karate API suite' }
    When method put
    Then status 200
    And match response.success == true
    And match response.message == 'Subject updated successfully.'
    And match response.data contains
      """
      {
        subjectId: '#(subjectId)',
        name: '#(updatedName)',
        description: 'Updated by the Karate API suite'
      }
      """

    Given path 'api/v1/subjects', subjectId
    When method delete
    Then status 200
    And match response == { success: true, message: 'Subject deleted successfully.', data: null }

    Given path 'api/v1/subjects', subjectId
    When method get
    Then status 404
    And match response.success == false
    And match response.message == 'Subject not found with id : ' + subjectId
    And match response.data == null

    Given path 'api/v1/subjects', subjectId
    And request { name: 'Updated after deletion' }
    When method put
    Then status 404
    And match response.success == false
    And match response.message == 'Subject not found with id : ' + subjectId
    And match response.data == null

    Given path 'api/v1/subjects', subjectId
    When method delete
    Then status 404
    And match response.success == false
    And match response.message == 'Subject not found with id : ' + subjectId
    And match response.data == null
