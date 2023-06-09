package com.example.spe_mini.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Activity4 {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int a4_id;
    private String name;
    private String achievement;
    private Date date;
    private String remark;
}
