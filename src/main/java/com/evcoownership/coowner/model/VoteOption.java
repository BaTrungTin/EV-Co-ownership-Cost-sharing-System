package com.evcoownership.coowner.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vote_options")
public class VoteOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vote_id")
    private Vote vote;

    @Column(nullable = false)
    private String option; // YES, NO, OPTION_1, OPTION_2, ...

    @Column(nullable = false)
    private Integer count; // Số phiếu bầu cho option này

    public Long getId() { return id; }
    public Vote getVote() { return vote; }
    public void setVote(Vote vote) { this.vote = vote; }
    public String getOption() { return option; }
    public void setOption(String option) { this.option = option; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}

