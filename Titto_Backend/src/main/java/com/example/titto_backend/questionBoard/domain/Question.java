package com.example.titto_backend.questionBoard.domain;

import com.example.titto_backend.auth.domain.User;
import com.example.titto_backend.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "author")
    private User author;

    @Setter
    @Column(name = "status")
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "department")
    private Department department;

    @Column(name = "question_title", nullable = false)
    private String title;

    @Column(name = "question_content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "send_Experience", nullable = false)
    private Integer sendExperience;

    // 채택된 답변 ID
    @Setter
    @OneToOne
    @JoinColumn(name = "accepted_answer")
    private Answer acceptedAnswer;

    @Setter
    @JoinColumn(name = "is_accepted_answer")
    private boolean isAnswerAccepted;

    public void update(String title, String content, Department department, Status status) {
        this.title = title;
        this.content = content;
        this.department = department;
        this.status = status;
    }

    @Column(name = "view_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer viewCount;

    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    private List<Answer> answers = new ArrayList<>();

    public void addViewCount() {
        this.viewCount++;
    }
}
