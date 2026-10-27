Feature: Trainee registration
  As a gym visitor
  I want to register as a trainee
  So that I can book trainings with a trainer

  Scenario: Successful registration with all details
    Given no trainee is registered with the name "Emma" "Watson"
    When I register a trainee with first name "Emma", last name "Watson", date of birth "1995-05-20" and no address
    Then the registration response status is 201
    And the response contains a generated username starting with "Emma.Watson"
    And the response contains a generated password of 10 characters

  Scenario: Successful registration without optional fields
    Given no trainee is registered with the name "Liam" "Brown"
    When I register a trainee with first name "Liam", last name "Brown", no date of birth and no address
    Then the registration response status is 201
    And the response contains a generated username starting with "Liam.Brown"

  Scenario: Registration is rejected when the first name is blank
    When I register a trainee with first name "", last name "Brown", no date of birth and no address
    Then the registration response status is 400

  Scenario: Registration is rejected when the date of birth is in the future
    When I register a trainee with first name "Noah", last name "Green", date of birth "2999-01-01" and no address
    Then the registration response status is 400

  Scenario: A second trainee with the same name gets a disambiguated username
    Given no trainee is registered with the name "Ava" "Stone"
    And a trainee is already registered with first name "Ava" and last name "Stone"
    When I register a trainee with first name "Ava", last name "Stone", no date of birth and no address
    Then the registration response status is 201
    And the response contains a generated username starting with "Ava.Stone1"
