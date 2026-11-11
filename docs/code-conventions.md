# Naming and documentation

## Names communicate responsibility

| Previous form | Current form | Reason |
| --- | --- | --- |
| `Request.AddTraining` | `ScheduleTrainingRequest` | Names the actual use case in code and OpenAPI. |
| `Request.UpdateTraineeTrainers` | `ReplaceTrainerAssignmentsRequest` | Makes replacement and empty-list clearing semantics explicit. |
| `Response.CreatedUser` | `RegistrationCredentials` | Identifies the sensitive one-time credential response. |
| `Profile` / `Summary.Training` | `UserProfile` / `TrainingSummary` | Distinguishes internal read projections from HTTP response records. |
| `TrainingServiceImpl` | `TrainingApplicationService` | Describes orchestration and transaction responsibility. |
| `UserService` | `PasswordService` | Its sole responsibility is changing passwords. |
| `LoginServiceImpl` | `JwtLoginService` | Identifies the authentication mechanism. |
| `WorkloadServiceImpl` | `TrainerWorkloadApplicationService` | Names both the domain and architectural responsibility. |
| `MonthSummary` / `YearSummary` | `MonthlyWorkload` / `YearlyWorkload` | Names the persisted projection buckets. |
| `trainingsSummaryDuration` | `durationMinutes` | States the measured quantity and its unit. |
| `DBCredentialGenerator` | `DatabaseCredentialGenerator` | Uses a readable, consistently capitalized class name. |
| `Validate` / `Validator` | `ValidateRequest` / `InputValidator` | Distinguishes application validation from Jakarta's validator types. |

Request records live in `dto.request`, response records in `dto.response`, and internal projections in `dto`. Application services implement narrow interfaces; `Impl` packages are not required. The custom Spring Data repository fragments retain their framework discovery names, including the `Impl` suffix.

Java type names changed without renaming the established request JSON properties. OpenAPI schema component names therefore change. MongoDB's existing nested `trainingsSummaryDuration` storage key is preserved through an explicit `@Field` mapping; the Java accessor becomes `getDurationMinutes`. Historical Flyway migrations remain unchanged so deployed checksums stay valid.

## Documentation policy

- Use JavaDoc for externally used service operations, request/response contracts, persistence compatibility, units, and meaningful failure boundaries.
- Keep application source free of inline narration and commented-out implementations. Express intent through method/type names and small operations.
- Put architectural tradeoffs, delivery limitations, operational procedures, and upgrade instructions in `docs/`.
- Preserve generated wrapper notices, executable shebangs, and previously applied migration contents.
- Do not document an outbox, deduplication, or security guarantee until code and failure tests demonstrate it.

See [architecture decisions](architecture.md), [dependency and image updates](upgrades.md), and the [root README](../README.md).

## Repository scripts

Run `scripts/generate-dev-keys.sh` from the root to create a missing development RSA keystore and export its matching workload public key. Existing API keys are retained; exporting the public key keeps both services aligned. Production uses external keys.

`./concat.sh [source-directory] [output-file]` creates a textual source/configuration dump for local review, excluding build, VCS, and IDE directories. The default output is `project_dump.txt`; review configuration content before sharing it.
