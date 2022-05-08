package com.rest.testrest.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "userinfo")
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 300, nullable = true)
    private String name;

    @Column(length = 300, unique = true, nullable = true)
    private String regNo;

    @Column(length = 300, nullable = true)
    private String userId;

    @Column(length = 16, nullable = true)
    private int refund;

    @Column(length = 16, nullable = true)
    private int deduction;

}
