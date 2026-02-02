package dorm.model;

public enum ApplicationStatus {
    // Phase One statuses
    PHASE_ONE_PENDING,      // waiting for review
    PHASE_ONE_APPROVED,     // Phase one approved
    PHASE_ONE_DECLINED,     // Application declined
    PHASE_ONE_RESUBMIT,     // Asked to resubmit with corrections
    
    // Phase Two statuses
    PHASE_TWO_PENDING,      // waiting for verification
    PHASE_TWO_APPROVED,     // Payment & details accepted
    PHASE_TWO_DECLINED,     //  Payment & details accepted :)
    
    // Final status
    ASSIGNED                // Building assigned, application complete
}
