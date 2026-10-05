package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_affiliations")
public class UserAffiliationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Column(name = "eps_id", nullable = false)
    public Short epsId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "eps_id", insertable = false, updatable = false)
    public EpsEntity eps;

    @Column(name = "eps_plan_id", nullable = false)
    public Short epsPlanId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "eps_plan_id", insertable = false, updatable = false)
    public EpsPlanEntity epsPlan;

    @Column(name = "regimen_id", nullable = false)
    public Short regimenId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "regimen_id", insertable = false, updatable = false)
    public RegimenEntity regimen;

    @Column(nullable = false)
    public boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt = Instant.now();
}
