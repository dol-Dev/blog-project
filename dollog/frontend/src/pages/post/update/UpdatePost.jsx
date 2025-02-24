import ImageResize from "quill-image-resize-module-react";
import React, { useEffect, useRef, useState } from "react";
import { Form } from "react-bootstrap";
import ReactQuill, { Quill } from "react-quill";
import "react-quill/dist/quill.snow.css";
import { useNavigate, useParams } from "react-router-dom";
import { Button, Dropdown, DropdownItem, DropdownMenu, Icon } from "semantic-ui-react";
import Swal from "sweetalert2";
import { toast } from "react-toastify"; 
import { useAuth } from "../../../contexts/AuthContext";
import axiosInstance from "../../../utils/axiosInstance";
import styles from "./updatePost.module.css";

// Quill 관련 설정
Quill.register("modules/imageResize", ImageResize);
const modules = {
    toolbar: {
        container: [
            [{ header: [1, 2, 3, false] }],
            ["bold", "italic", "underline", "strike"],
            ["blockquote"],
            [{ list: "ordered" }, { list: "bullet" }],
            [{ color: [] }, { background: [] }],
            [{ align: [] }, "link", "image"],
        ],
    },
    imageResize: {
        parchment: Quill.import("parchment"),
        modules: ["Resize", "DisplaySize", "Toolbar"],
    },
};

// 현재 게시글 수정 시 해당 게시글의 카테고리도 바꿀 수 있는건 곧 구현 예정
const UpdatePost = () => {
    const [detailPost, setDetailPost] = useState({});
    const navigate = useNavigate();
    const { id } = useParams();
    const { authInfo } = useAuth();
    const quillRef = useRef(null);

    useEffect(() => {
        if (authInfo) {
            fetchPosts();
        }
    }, [authInfo]);

    // 해당 게시글 조회 
    const fetchPosts = async () => {
        try {
            const response = await axiosInstance.get(`/api/posts/${id}`);
            setDetailPost(response.data.data);
        } catch (error) {
            toast.error("Failed to fetch posts.");
        }
    };

    if (!detailPost.title && !detailPost.content && !id) {
        return toast.info("Loading...");
    }

    // 게시글 수정
    const handleUpdatePost = async () => {
        try {
            const response = await axiosInstance.put(`/api/posts/${id}`, {
                title: detailPost.title,
                content: detailPost.content,
                categoryId: detailPost.category.id,
            });

            if (response.status === 200) {
                navigate(`/detail-post/${id}`);
            }
        } catch (error) {
            toast.error("수정 실패. 다시 시도해주세요.");
        }
    };

    // 게시글 삭제
    const handleDeletePost = async () => {
        const result = await Swal.fire({
            title: "정말 삭제하시겠습니까?",
            text: "삭제 후에는 복구할 수 없습니다!",
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "삭제",
            cancelButtonText: "취소",
        });

        if (result.isConfirmed) {
            try {
                await axiosInstance.delete(`/api/psosts/${id}`);
                navigate("/");
            } catch (error) {
                toast.error("삭제 실패. 다시 시도해주세요.");
            }
        }
    };

    return (
        <>
            <br />
            <br />
            <div className={styles.container}>
                <Form>
                    <Form.Group>
                        <Dropdown
                            placeholder="Select a category"
                            fluid
                            pointing
                            className={`item ${styles.dropdown}`}
                            text={detailPost.category?.name || "Select a category"}
                        />
                    </Form.Group>
                    <br />
                    <Form.Group>
                        <Form.Control
                            type="text"
                            value={detailPost.title}
                            onChange={(e) =>
                                setDetailPost({
                                    ...detailPost,
                                    title: e.target.value,
                                })
                            }
                        />
                    </Form.Group>
                    <br />
                    <Form.Group>
                        <ReactQuill
                            ref={quillRef}
                            theme="snow"
                            modules={modules}
                            className={styles["quill-editor"]}
                            value={detailPost.content}
                            onChange={(value) =>
                                setDetailPost({
                                    ...detailPost,
                                    content: value,
                                })
                            }
                        />
                    </Form.Group>
                    <br />
                    <br />
                    <br />
                    <br />
                    <div className={styles["button-group"]}>
                        <Button icon onClick={() => navigate("/")}>
                            <Icon name="arrow left" />
                        </Button>
                        <Button icon type="button" onClick={handleUpdatePost}>
                            <Icon name="cut" />
                        </Button>
                        <Button icon type="button" onClick={handleDeletePost}>
                            <Icon name="trash alternate" />
                        </Button>
                    </div>
                    <br />
                    <br />
                </Form>
            </div>
        </>
    );
};

export default UpdatePost;
