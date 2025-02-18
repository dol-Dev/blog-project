package com.doldev.dollog.domain.post.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.category.entity.Category;
import com.doldev.dollog.domain.comment.entity.Comment;
import com.doldev.dollog.domain.like.entity.Like;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private int count;

    @ColumnDefault("0")
    private int likeCnt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Like> likes;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    @OrderBy("id DESC")
    private List<Comment> comments;

    @CreationTimestamp
    private LocalDateTime createDate;

    @UpdateTimestamp
    private LocalDateTime modifyDate;

    /* 포스트 정보 변경 메서드들 */
    public void updateTitle(String newTitle) {
        if (StringUtils.isNotBlank(newTitle)) {
            this.title = newTitle;
        }
    }

    public void updateContent(String newContent) {
        if (StringUtils.isNotBlank(newContent)) {
            this.content = newContent;
        }
    }

    public void incrementLikeCnt() {
        this.likeCnt++;
    }

    public void decrementLikeCnt() {
        this.likeCnt = Math.max(0, this.likeCnt - 1);
    }

    /* 연관관계 설정 메서드들 */
    public void assignCategory(Category category) {
        if (category != null) {
            this.category = category;
        }
    }
}
