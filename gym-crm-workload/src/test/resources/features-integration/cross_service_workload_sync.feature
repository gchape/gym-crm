Feature: gym-crm to gym-crm-workload workload synchronization
  As the gym-crm platform
  I want events published by gym-crm's WorkloadEventPublisher to be reliably
  consumed and aggregated by gym-crm-workload over the real Kafka broker
  So that a trainer's workload is always accurate regardless of which service
  most recently touched their schedule

  These scenarios reproduce gym-crm's exact producer contract (topic,
  partition key, transactionId header, JSON payload shape) rather than
  calling gym-crm-workload's internals directly, so a schema or header
  change on the producing side would be caught here.

  Scenario: A training added in gym-crm is reflected in gym-crm-workload's API
    Given no workload summary exists in gym-crm-workload for trainer "svetlana.orlova"
    When gym-crm publishes a workload ADD event with transaction id "tx-1001" for trainer "svetlana.orlova" ("Svetlana" "Orlova", active) on "2025-10-05" for 50 minutes
    Then within 10 seconds gym-crm-workload's API reports 50 minutes for trainer "svetlana.orlova" in 2025-10

  Scenario: Sequential ADD then DELETE events from gym-crm net out correctly
    Given no workload summary exists in gym-crm-workload for trainer "pavel.orlov"
    When gym-crm publishes a workload ADD event with transaction id "tx-2001" for trainer "pavel.orlov" ("Pavel" "Orlov", active) on "2025-11-03" for 90 minutes
    And gym-crm publishes a workload DELETE event with transaction id "tx-2002" for trainer "pavel.orlov" ("Pavel" "Orlov", active) on "2025-11-03" for 30 minutes
    Then within 10 seconds gym-crm-workload's API reports 60 minutes for trainer "pavel.orlov" in 2025-11

  Scenario: A business-invalid event for one trainer does not block a valid event for another
    Given no workload summary exists in gym-crm-workload for trainer "invalid.trainer.two"
    And no workload summary exists in gym-crm-workload for trainer "valid.trainer.two"
    When gym-crm publishes a workload ADD event with transaction id "tx-3001" for trainer "invalid.trainer.two" ("In" "Valid", active) on "2025-12-01" for 0 minutes
    And gym-crm publishes a workload ADD event with transaction id "tx-3002" for trainer "valid.trainer.two" ("Val" "Idsson", active) on "2025-12-01" for 25 minutes
    Then within 10 seconds gym-crm-workload's API reports 25 minutes for trainer "valid.trainer.two" in 2025-12
    And gym-crm-workload still has no workload summary for trainer "invalid.trainer.two" after 5 seconds
