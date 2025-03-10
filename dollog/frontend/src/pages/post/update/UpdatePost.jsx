// UpdatePost.jsx
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

    // 기존 게시글 데이터
    const [detailPost, setDetailPost] = useState({
        title: '',
        content: '',
        category: null,
    });

    // 전체 카테고리
    const [categoryData, setCategoryData] = useState([]);

    // 부모/자식 카테고리 상태
    const [selectedParentCategory, setSelectedParentCategory] = useState(null);
    const [subCategories, setSubCategories] = useState([]);
    const [selectedChildCategory, setSelectedChildCategory] = useState(null);

    useEffect(() => {
        if (authInfo) {
            fetchPost();
            fetchCategories(authInfo.nickname);
        }
    }, [authInfo, id]);

    useEffect(() => {
        if (quillRef.current) {
            const editor = quillRef.current.getEditor();
            setQuillInstance(editor);
        }
    }, [quillRef, setQuillInstance]);

    // 게시글 상세 불러오기
    const fetchPost = async () => {
        try {
            const response = await axiosInstance.get(`/api/posts/${id}`);
            const post = response.data?.data;
            setDetailPost(post);

            // 기존 카테고리가 있다면 parentCategory로 셋팅
            if (post?.category) {
                setSelectedParentCategory({
                    label: post.category.name,
                    value: post.category.id,
                });
            }
        } catch (error) {
            toast.error('게시글 불러오기에 실패했습니다.');
        }
    };

    // 카테고리 목록 불러오기
    const fetchCategories = async (nickname) => {
        try {
            const response = await axiosInstance.get(`/api/categories/nickname`, { params: { nickname } });
            setCategoryData(response.data.data || []);
            return response.data.data || [];
        } catch (error) {
            console.error('Failed to fetch categories:', error);
            return [];
        }
    };


    // 부모 카테고리 선택
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

    // 부모 카테고리 즉석 생성
    const handleCreateParentCategory = (inputValue) => {
        if (!authInfo) {
            toast.error("로그인 정보가 없습니다.");
            return;
        }

        const newCategoryData = { name: inputValue };

        axiosInstance.post('/api/categories', newCategoryData)
            .then(async () => {
                toast.success("부모 카테고리가 생성되었습니다.");

                // 전체 목록 재조회
                const updatedList = await fetchCategories(authInfo.nickname);

                // 방금 만든 parent를 찾고 선택
                const justCreated = updatedList.find(cat => cat.name === inputValue);
                if (justCreated) {
                    setSelectedParentCategory({ label: justCreated.name, value: justCreated.id });
                    setSubCategories([]);
                    setSelectedChildCategory(null);
                }
            })
            .catch(error => {
                console.error('Failed to create parent category:', error);
                toast.error("카테고리 생성 실패");
            });
    };

    // **자식 카테고리 즉석 생성
    const handleCreateChildCategory = (inputValue) => {
        if (!authInfo) {
            toast.error("로그인 정보가 없습니다.");
            return;
        }
        if (!selectedParentCategory) {
            toast.error("부모 카테고리를 먼저 선택해주세요.");
            return;
        }

        const newChildData = {
            name: inputValue,
            parentId: selectedParentCategory.value,
        };

        axiosInstance.post('/api/categories', newChildData)
            .then(async () => {
                toast.success("자식 카테고리가 생성되었습니다.");

                // 전체 목록 재조회
                const updatedList = await fetchCategories(authInfo.nickname);

                // parent 찾고, 그 children에서 막 만든 자식 찾기
                const parentCat = updatedList.find(cat => cat.id === selectedParentCategory.value);
                const children = parentCat?.children || [];
                const justCreatedChild = children.find(ch => ch.name === inputValue);

                if (justCreatedChild) {
                    // subCategories 세팅
                    const mappedChildren = children.map(ch => ({ label: ch.name, value: ch.id }));
                    setSubCategories(mappedChildren);

                    // 새 자식카테고리 선택
                    setSelectedChildCategory({ label: justCreatedChild.name, value: justCreatedChild.id });
                }
            })
            .catch(error => {
                console.error('Failed to create child category:', error);
                toast.error("자식 카테고리 생성 실패");
            });
    };

    // 게시글 수정
    const handleUpdatePost = async () => {
        try {
            const categoryId =
                selectedChildCategory?.value ||
                selectedParentCategory?.value ||
                detailPost.category?.id ||
                null;

            const response = await axiosInstance.put(`/api/posts/${id}`, {
                title: detailPost.title,
                content: detailPost.content,
                categoryId,
            });
            if (response.status === 200) {
                toast.success("게시글이 수정되었습니다.");
                navigate(`/detail-post/${id}`);
            }
        } catch (error) {
            toast.error('수정 실패. 다시 시도해주세요.');
        }
    };

    // 게시글 삭제
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

            {/* 부모 카테고리 + 즉석 생성 */}
            <Form.Group>
                <CreatableSelect
                    placeholder="부모 카테고리 선택"
                    options={categoryData.map(cat => ({
                        label: cat.name,
                        value: cat.id,
                    }))}
                    value={selectedParentCategory}
                    onChange={handleParentCategorySelect}
                    onCreateOption={handleCreateParentCategory}
                    className={styles.dropdown}
                />
            </Form.Group>

            {/* 자식 카테고리 선택 (있으면) + 즉석 생성 */}
            {subCategories.length > 0 && (
                <Form.Group>
                    <CreatableSelect
                        placeholder="자식 카테고리 선택"
                        options={subCategories}
                        value={selectedChildCategory}
                        onChange={handleChildCategorySelect}
                        onCreateOption={handleCreateChildCategory}
                        className={styles.dropdown}
                    />
                </Form.Group>
            )}

            <Form.Group>
                <Form.Control
                    className={styles.title}
                    type="text"
                    placeholder="제목을 입력하세요"
                    value={detailPost.title}
                    onChange={(e) => setDetailPost({ ...detailPost, title: e.target.value })}
                />
            </Form.Group>

            <Form.Group>
                <ReactQuill
                    ref={quillRef}
                    theme="snow"
                    modules={modules}
                    formats={formats}
                    className={styles['quill-editor']}
                    value={detailPost.content}
                    onChange={(value) => setDetailPost({ ...detailPost, content: value })}
                />
            </Form.Group>

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
