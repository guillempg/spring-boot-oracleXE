Feature: Student registration and deletion

  Background:
    Given the app is running
    And users authorization is configured
    And 'admin' user 'nickfury' logs into the application with password 'test1'
    And user 'nickfury' retrieves external ids for users:
      | hulk      |
      | antman    |
      | deadpool  |
      | spiderman |

  Scenario: Create new student
    When admin user 'nickfury' successfully register student with details:
      | name      | courses                           |
      | antman    | Explosives 101, Rocket riding 101 |
      | spiderman | Explosives 101                    |

  Scenario: Delete student and her course registrations
    Given 'nickfury' successfully register student with username antman on courses 'Dodge Missiles, Fly over Cliffs'
    When 'nickfury' submits a request to delete student antman
    Then 'nickfury' verifies student antman and her courses registrations are deleted

  Scenario: List all students enrolled to a course
    Given admin user 'nickfury' successfully register student with details:
      | name      | courses                           | phones                  |
      | antman    | Explosives 101, Rocket riding 101 | 555-111-222             |
      | spiderman | Explosives 101                    | 555-333-333,555-444-444 |
      | deadpool  | Explosives 101, Fly over cliffs   |                         |
    When 'nickfury' request the list of students enrolled to course 'Explosives 101':
      | antman    |
      | deadpool  |
      | spiderman |

    ## This one uses testcontainer with rabbitmq
  Scenario: Create new student from messaging queue
    When we registers students via messaging with details:
      | name      | courses                           |
      | antman    | Explosives 101, Rocket riding 101 |
      | spiderman | Explosives 101                    |
    Then 'nickfury' verifies that students exist with the following names:
      | antman    |
      | spiderman |

    ## this scenario uses spring integration message channel to delete student registration
  Scenario: Delete student from messaging queue
    Given admin user 'nickfury' successfully register student with details:
      | name      | courses                           |
      | antman    | Explosives 101, Rocket riding 101 |
      | spiderman | Explosives 101                    |
    When student with name antman is deleted via messaging
    Then 'nickfury' verifies student antman and her courses registrations are deleted

  Scenario: List students not registered to a course
    Given admin user 'nickfury' successfully register student with details:
      | name      | courses                           | keycloakId  |
      | antman    | Explosives 101, Rocket riding 101 | 111-111-111 |
      | spiderman | Explosives 101                    | 222-222-222 |
      | deadpool  | Explosives 101, Fly over cliffs   | 333-333-333 |
    When 'nickfury' lists students not registered to course 'Rocket riding 101':
      | spiderman |
      | deadpool  |

  Scenario: Save student score
    Given 'nickfury' saves teacher hulk
    And 'nickfury' assigns teacher with details:
      | name | coursesAssigned                   |
      | hulk | Explosives 101, Rocket riding 101 |
    And 'teacher' user 'hulk' logs into the application with password 'test1'
    And admin user 'nickfury' successfully register student with details:
      | name      | courses                           |
      | antman    | Explosives 101, Rocket riding 101 |
      | spiderman | Rocket riding 101                 |
    When hulk registers student scores:
      | studentName | courseName        | score |
      | antman      | Explosives 101    | 4.99  |
      | antman      | Rocket riding 101 | 5.99  |
      | spiderman   | Rocket riding 101 | 6.98  |
    Then teacher hulk sees student scores for course 'Rocket riding 101':
      | studentName | courseName        | score |
      | antman      | Rocket riding 101 | 5.99  |
      | spiderman   | Rocket riding 101 | 6.98  |
    And 'student' user 'antman' logs into the application with password 'test1'
    And student antman sees student scores:
      | studentName | courseName        | score |
      | antman      | Explosives 101    | 4.99  |
      | antman      | Rocket riding 101 | 5.99  |
    And 'student' user 'spiderman' logs into the application with password 'test1'
    And student spiderman sees student scores:
      | studentName | courseName        | score |
      | spiderman   | Rocket riding 101 | 6.98  |