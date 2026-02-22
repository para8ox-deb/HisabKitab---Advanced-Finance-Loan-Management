package com.hisabkitab.entity;

import com.hisabkitab.enums.BorrowerStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Borrower entity - Maps to the "borrowers" table in MySQL.
 *
 * Each borrower belongs to ONE lender (the User who created them).
 * This is a Many-to-One relationship:
 *   Many borrowers → One lender
 *
 * Data isolation: A lender can only see/manage their OWN borrowers.
 */
@Entity
@Table(name = "borrowers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Borrower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Borrower name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 15, message = "Phone number must be at most 15 characters")
    private String phone;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @Size(max = 500, message = "Notes must be at most 500 characters")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowerStatus status = BorrowerStatus.ACTIVE;

    /**
     * Many-to-One relationship: Many borrowers belong to one lender (User).
     *
     * @ManyToOne  → This entity is the "many" side
     * @JoinColumn → Creates a foreign key column "lender_id" pointing to users.id
     *
     * LAZY fetch: Don't load the full User object unless explicitly accessed.
     * This is a performance optimization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;

    /**
     * Optional One-to-One link to a User account with BORROWER role.
     *
     * When a borrower registers on the platform, their User account
     * is linked here. This allows them to log in and view their own
     * loans and EMI schedules through the Borrower Portal.
     *
     * This field is OPTIONAL — a borrower record can exist without
     * a linked user account (lender-created borrower without portal access).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_user_id", nullable = true, unique = true)
    private User linkedUser;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
