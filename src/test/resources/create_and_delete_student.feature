Feature: Create and delete student

  Scenario: Create new student
    Given the app is running
    Then we successfully register student with details:
      | name            | courses                           | ssn         |
      | Wiley E. Coyote | Explosives 101, Rocket riding 101 | 111-111-111 |
      | Tasmanian Devil | Explosives 101                    | 222-222-222 |

  Scenario: Delete student and her course registrations
    Given the app is running
    And we successfully register student with name 'Road Runner' and ssn '555-555-555' on courses 'Dodge Missiles, Fly over Cliffs'
    When we submit a request to delete student with ssn '555-555-555'
    Then the student with ssn '555-555-555' and her courses registrations are deleted

  Scenario: List all students, sorted by their name, enrolled to a course
    Given the app is running
    And we successfully register student with details:
      | name            | courses                           | ssn         |
      | Wiley E. Coyote | Explosives 101, Rocket riding 101 | 111-111-111 |
      | Tasmanian Devil | Explosives 101                    | 222-222-222 |
      | Road Runner     | Explosives 101, Fly over cliffs   | 333-333-333 |
    When we request the list of students enrolled to course 'Explosives 101' we receive:
      | studentName     |
      | Road Runner     |
      | Tasmanian Devil |
      | Wiley E. Coyote |
