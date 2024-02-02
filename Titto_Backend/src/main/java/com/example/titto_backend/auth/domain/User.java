package com.example.titto_backend.auth.domain;

import com.example.titto_backend.auth.dto.request.SignUpDTO;
import com.example.titto_backend.common.BaseEntity;
import com.example.titto_backend.matchingBoard.domain.matchingBoard.MatchingPost;
import com.example.titto_backend.matchingBoard.domain.review.MatchingPostReview;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long id;

  @Column(name = "email")
  @NotBlank(message = "이메일은 필수!")
  private String email;

  @Column(name = "profile", nullable = true)
  private String profile;

  @Setter
  @Column(name = "name")
  private String name;

  @Setter
  @Column(name = "nickname")
  private String nickname;

  @Setter
  @Column(name = "student_no")
  private String studentNo;

  @Setter
  @Column(name = "department")
  private String department;

  @Column(name = "social_id")
  private String socialId;

  @Enumerated(EnumType.STRING)
  private SocialType socialType;

  @Setter
  @Column(name = "one_line_intro")
  private String oneLineIntro;

  @Setter
  @Column(name = "self_intro", columnDefinition = "TEXT")
  private String selfIntro;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<Badge> badges;

//  //post count
//  private Integer postCount;

//  //available point
//  private Integer availablePoint;

  @JsonIgnore
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<MatchingPost> matchingPosts;

  @JsonIgnore
  @OneToMany(mappedBy = "reviewAuthor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<MatchingPostReview> matchingPostReviews;

  @Builder
  public User(String email, String profile, String socialId, SocialType socialType) {
    this.email = email;
    this.profile = profile;
    this.socialId = socialId;
    this.socialType = socialType;
  }

  public void signupUser(SignUpDTO signUpDTO) {
    this.setName(signUpDTO.getName());
    this.setNickname(signUpDTO.getNickname());
    this.setStudentNo(signUpDTO.getStudentNo());
    this.setDepartment(signUpDTO.getDepartment());
  }
}
