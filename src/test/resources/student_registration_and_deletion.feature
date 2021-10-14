Feature: Student registration and deletion

  Scenario: Create new student
    Given the app is running
    Then we successfully register student with details:
      | name            | courses                           | ssn         | phones                  |
      | Wiley E. Coyote | Explosives 101, Rocket riding 101 | 111-111-111 | 555-111-222             |
      | Tasmanian Devil | Explosives 101                    | 222-222-222 | 555-333-333,555-444-444 |

  Scenario: Delete student and her course registrations
    Given the app is running
    And we successfully register student with name 'Road Runner' and ssn '555-555-555' and phones '' on courses 'Dodge Missiles, Fly over Cliffs'
    When we submit a request to delete student with ssn '555-555-555'
    Then the student with ssn '555-555-555' and her courses registrations are deleted

  Scenario: List all students, sorted by their name, enrolled to a course
    Given the app is running
    And we successfully register student with details:
      | name            | courses                           | ssn         | phones                  |
      | Wiley E. Coyote | Explosives 101, Rocket riding 101 | 111-111-111 | 555-111-222             |
      | Tasmanian Devil | Explosives 101                    | 222-222-222 | 555-333-333,555-444-444 |
      | Road Runner     | Explosives 101, Fly over cliffs   | 333-333-333 |                         |
    When we request the list of students enrolled to course 'Explosives 101' we receive:
      | studentName     |
      | Road Runner     |
      | Tasmanian Devil |
      | Wiley E. Coyote |

    ## This one uses testcontainer with rabbitmq
  Scenario: Create new student from messaging queue
    Given the app is running
    When we register students via messaging with details:
      | name            | courses                           | ssn         |
      | Wiley E. Coyote | Explosives 101, Rocket riding 101 | 111-111-111 |
      | Tasmanian Devil | Explosives 101                    | 222-222-222 |
    Then students should exits with following security social numbers:
      | ssn         |
      | 111-111-111 |
      | 222-222-222 |

    ## this scenario uses spring integration message channel
  Scenario: Delete student from messaging queue
    Given the app is running
    And we successfully register student with details:
      | name            | courses                           | ssn         |
      | Wiley E. Coyote | Explosives 101, Rocket riding 101 | 111-111-111 |
      | Tasmanian Devil | Explosives 101                    | 222-222-222 |
      | Road Runner     | Explosives 101, Fly over cliffs   | 333-333-333 |
    When we receive a delete student with ssn '111-111-111' message
    Then the student with ssn '111-111-111' and her courses registrations are deleted

  Scenario: List students not registered to a course
    Given the app is running
    And we successfully register student with details:
      | name            | courses                           | ssn         |
      | Wiley E. Coyote | Explosives 101, Rocket riding 101 | 111-111-111 |
      | Tasmanian Devil | Explosives 101                    | 222-222-222 |
      | Road Runner     | Explosives 101, Fly over cliffs   | 333-333-333 |
    When we list students not registered to course 'Rocket riding 101' we get:
      | studentName     |
      | Tasmanian Devil |
      | Road Runner     |