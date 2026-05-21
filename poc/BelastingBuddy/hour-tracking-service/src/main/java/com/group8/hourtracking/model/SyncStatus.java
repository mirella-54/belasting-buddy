package com.group8.hourtracking.model;

/**
 * Enum representing the status of a sync operation (with third-party tool)
 */
public enum SyncStatus {

  /** Sync operation completed successfully. */
  SUCCESS,

  /** Sync operation failed. */
  FAILED,

  /** Sync operation is currently in progress. */
  IN_PROGRESS
}
