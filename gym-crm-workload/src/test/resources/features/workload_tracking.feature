Feature: Trainer workload tracking
  As the gym-crm-workload service
  I want to aggregate a trainer's monthly training minutes from Kafka events
  So that the workload summary API always reflects the trainer's schedule

  Scenario: A single ADD event creates a new monthly summary
    Given no workload summary exists for trainer "irina.popova"
    When an ADD workload event is published for trainer "irina.popova" ("Irina" "Popova", active) on "2025-06-10" for 60 minutes
    Then within 10 seconds trainer "irina.popova" has a workload summary
    And within 10 seconds the summary for trainer "irina.popova" year 2025 month 6 shows 60 minutes

  Scenario: A second ADD event in the same month accumulates duration
    Given no workload summary exists for trainer "oleg.kuzmin"
    And an ADD workload event has already been processed for trainer "oleg.kuzmin" ("Oleg" "Kuzmin", active) on "2025-07-01" for 40 minutes
    When an ADD workload event is published for trainer "oleg.kuzmin" ("Oleg" "Kuzmin", active) on "2025-07-15" for 20 minutes
    Then within 10 seconds the summary for trainer "oleg.kuzmin" year 2025 month 7 shows 60 minutes

  Scenario: A DELETE event reduces the accumulated duration but never goes below zero
    Given no workload summary exists for trainer "maria.volkova"
    And an ADD workload event has already been processed for trainer "maria.volkova" ("Maria" "Volkova", active) on "2025-08-05" for 30 minutes
    When a DELETE workload event is published for trainer "maria.volkova" ("Maria" "Volkova", active) on "2025-08-05" for 100 minutes
    Then within 10 seconds the summary for trainer "maria.volkova" year 2025 month 8 shows 0 minutes

  Scenario: Requesting a workload summary for an unknown trainer returns 404
    When I request the workload summary for trainer "no.such.trainer"
    Then the workload response status is 404

  Scenario: An event with a non-positive duration is rejected and not persisted
    Given no workload summary exists for trainer "invalid.trainer"
    When an ADD workload event is published for trainer "invalid.trainer" ("In" "Valid", active) on "2025-09-01" for 0 minutes
    Then trainer "invalid.trainer" still has no workload summary after 5 seconds
