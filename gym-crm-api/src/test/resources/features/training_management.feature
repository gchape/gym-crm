Feature: Training management
  As a trainer
  I want to schedule and cancel trainings with my trainees
  So that the gym's schedule reflects reality

  Background:
    Given the training type "CARDIO" exists
    And a trainee "Chris" "Miller" is registered
    And a trainer "Dana" "Ford" is registered with specialization "CARDIO"

  Scenario: A trainer successfully schedules a training with an existing trainee
    When trainer "Dana.Ford" schedules a "Cardio Blast" training with trainee "Chris.Miller" on "2999-01-10" for 45 minutes
    Then the training response status is 200
    And trainee "Chris.Miller" has 1 training on record

  Scenario: Scheduling fails when the trainee does not exist
    When trainer "Dana.Ford" schedules a "Cardio Blast" training with trainee "Ghost.User" on "2999-01-10" for 45 minutes
    Then the training response status is 404

  Scenario: Scheduling fails when the duration exceeds the daily maximum
    When trainer "Dana.Ford" schedules a "Cardio Blast" training with trainee "Chris.Miller" on "2999-01-10" for 500 minutes
    Then the training response status is 400

  Scenario: Scheduling fails when the caller lacks the trainer role
    When an unauthenticated caller schedules a "Cardio Blast" training with trainee "Chris.Miller" and trainer "Dana.Ford" on "2999-01-10" for 45 minutes
    Then the training response status is 401

  Scenario: A trainer cancels a previously scheduled training
    Given trainer "Dana.Ford" has scheduled a "Cardio Blast" training with trainee "Chris.Miller" on "2999-01-10" for 45 minutes
    When trainer "Dana.Ford" cancels that training
    Then the training response status is 200
    And trainee "Chris.Miller" has 0 trainings on record

  Scenario: Cancelling an unknown training fails
    When trainer "Dana.Ford" cancels training id 999999
    Then the training response status is 404
