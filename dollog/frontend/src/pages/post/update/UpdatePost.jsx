import React, { useEffect, useRef, useState } from 'react';
import { Form } from 'react-bootstrap';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import { useNavigate, useParams } from 'react-router-dom';
import CreatableSelect from 'react-select/creatable';
import { toast } from 'react-toastify';
import { Button, Icon } from 'semantic-ui-react';
import Swal from 'sweetalert2';
import { useAuth } from '../../../contexts/AuthContext';
import { useQuill } from '../../../contexts/QuillContext';
import axiosInstance from '../../../utils/axiosInstance';
import styles from './updatePost.module.css';

const UpdatePost = () => {
    const { authInfo } = useAuth();
    const { setQuillInstance, modules, formats } = useQuill();
    const { id } = useParams();
    const navigate = useNavigate();
    const quillRef = useRef(null);

    // 기존 게시글 데이터 상태
    const [detailPost, setDetailPost] = useState({
        title: '',
        content: '',
        category: null,
    });

    // 카테고리 관련 상태
    const [categoryData, setCategoryData] = useState([]);
    const [selectedParentCategory, setSelectedParentCategory] = useState(null);
    const [selectedChildCategory, setSelectedChildCategory] = useState(null);
    const [subCategories, setSubCategories] = useState([]);

    // 게시글 및 카테고리 데이터 불러오기
    useEffect(() => {
        if (authInfo) {
            fetchPost();
            fetchCategories(authInfo?.nickname);
        }
    }, [authInfo, id]);

    // ReactQuill 인스턴스 등록 (WritePost와 동일)
    useEffect(() => {
        if (quillRef.current) {
            const editor = quillRef.current.getEditor();
            setQuillInstance(editor);
        }
    }, [quillRef, setQuillInstance]);

    // 기존 게시글 불러오기
    const fetchPost = async () => {
        try {
            const response = await axiosInstance.get(`/api/posts/${id}`);
            const post = response.data.data;
            setDetailPost(post);
            if (post.category) {
                setSelectedParentCategory({
                    label: post.category.name,
                    value: post.category.id,
                });
            }
        } catch (error) {
            toast.error('게시글 불러오기에 실패했습니다.');
        }
    };

    // 카테고리 데이터 불러오기
    const fetchCategories = async (nickname) => {
        try {
            const response = await axiosInstance.get(`api/categories/nickname`, {
                params: {nickname}
            });
            setCategoryData(response.data.data);
        } catch (error) {
            console.error('카테고리 불러오기 실패:', error);
        }
    };

    // 부모 카테고리 선택 시 자식 카테고리 매핑
    const handleParentCategorySelect = (selectedOption) => {
        setSelectedParentCategory(selectedOption);
        if (selectedOption) {
            const parentCat = categoryData.find(cat => cat.id === selectedOption.value);
            const children = parentCat?.children || [];
            const mappedChildren = children.map(child => ({
                label: child.name,
                value: child.id,
            }));
            setSubCategories(mappedChildren);
            setSelectedChildCategory(null);
        } else {
            setSubCategories([]);
            setSelectedChildCategory(null);
        }
    };

    // 자식 카테고리 선택
    const handleChildCategorySelect = (selectedOption) => {
        setSelectedChildCategory(selectedOption);
    };

    // 게시글 수정 처리
    const handleUpdatePost = async () => {
        try {
            // 자식 카테고리가 선택되었으면 우선 사용, 없으면 부모 카테고리 또는 기존 값 사용
            const categoryId =
                selectedChildCategory?.value ||
                selectedParentCategory?.value ||
                detailPost.category?.id;
            const response = await axiosInstance.put(`/api/posts/${id}`, {
                title: detailPost.title,
                content: detailPost.content,
                categoryId: categoryId,
            });
            if (response.status === 200) {
                navigate(`/detail-post/${id}`);
            }
        } catch (error) {
            toast.error('수정 실패. 다시 시도해주세요.');
        }
    };

    // 게시글 삭제 처리
    const handleDeletePost = async () => {
        const result = await Swal.fire({
            title: '정말 삭제하시겠습니까?',
            text: '삭제 후에는 복구가 불가능합니다!',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: '삭제',
            cancelButtonText: '취소',
        });
        if (result.isConfirmed) {
            try {
                const response = await axiosInstance.delete(`/api/posts/${id}`);
                if (response.status === 200) {
                    navigate(`/blog/${authInfo?.nickname}`, { state: authInfo?.blogName });
                }
            } catch (error) {
                toast.error('삭제 실패. 다시 시도해주세요.');
            }
        }
    };

    return (
        <Form className={styles['form-container']}>
            {/* 부모 카테고리 선택 */}
            <Form.Group>
                <CreatableSelect
                    placeholder="부모 카테고리 선택"
                    options={categoryData.map(cat => ({
                        label: cat.name,
                        value: cat.id,
                    }))}
                    value={selectedParentCategory}
                    onChange={handleParentCategorySelect}
                    className={styles.dropdown}
                />
            </Form.Group>

            {/* 자식 카테고리 선택 (있을 경우) */}
            {subCategories.length > 0 && (
                <Form.Group>
                    <CreatableSelect
                        placeholder="자식 카테고리 선택"
                        options={subCategories}
                        value={selectedChildCategory}
                        onChange={handleChildCategorySelect}
                        className={styles.dropdown}
                    />
                </Form.Group>
            )}

            {/* 제목 입력 */}
            <Form.Group>
                <Form.Control
                    className={styles.title}
                    type="text"
                    placeholder="제목을 입력하세요"
                    value={detailPost.title}
                    onChange={(e) =>
                        setDetailPost({ ...detailPost, title: e.target.value })
                    }
                />
            </Form.Group>

            {/* 내용 입력 (ReactQuill) */}
            <Form.Group>
                <ReactQuill
                    ref={quillRef}
                    theme="snow"
                    modules={modules}
                    formats={formats}
                    className={styles['quill-editor']}
                    value={detailPost.content}
                    onChange={(value) =>
                        setDetailPost({ ...detailPost, content: value })
                    }
                />
            </Form.Group>

            {/* 버튼 그룹 */}
            <div className={styles['button-group']}>
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
        </Form>
    );
};

export default UpdatePost;
