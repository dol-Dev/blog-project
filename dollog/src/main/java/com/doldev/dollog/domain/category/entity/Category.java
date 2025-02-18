package com.doldev.dollog.domain.category.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.post.entity.Post;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")                         
    private Category parent;

    @JsonManagedReference
    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)                                                             
    private List<Category> children = new ArrayList<>(); 

        // orderIndex -> 부모 카테고리 정렬용 필드
    @Column(name = "parent_order_index")
    private Integer parentOrderIndex;

    // orderIndex -> 자식 카테고리 정렬용 필드
    @Column(name = "child_order_index")
    private Integer childOrderIndex;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonBackReference
    @OneToMany(mappedBy = "category", cascade = CascadeType.REMOVE)
    private List<Post> posts = new ArrayList<>();

    // 게시물 수 계산
    @JsonGetter("postCount")
    public int getPostCount() {
        return posts.size();
    }
}
