Feature: Create and delete student

  Scenario: Create new student
    Given the app is running
    Then we successfully register student with details:
    | name             | courses                          |
    | Wiley E. Coyote  | Explosives 101, Rocket riding 101|
    | Tasmanian Devil  | Explosives 101                   |
