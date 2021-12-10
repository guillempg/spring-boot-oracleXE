Feature: Student registration and deletion via messaging

  Background:
    Given the app is running
    And 'admin' user 'nickfury' logs into the application with password 'test1'
    And user 'nickfury' retrieves external ids for users:
      | hulk      |
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
