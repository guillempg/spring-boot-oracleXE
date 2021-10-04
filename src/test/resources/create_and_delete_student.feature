Feature: Create and delete student


  Scenario: Create new student
    Given the app is running and connected to database
    Then we successfully create student with details:
    | name           | ID | courses                          |
    | Wiley Coyote   | 1  | Explosives 101, Rocket riding 101|