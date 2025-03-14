import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Form } from 'react-bootstrap';
import ReactQuill from 'react-quill-new';
import 'react-quill-new/dist/quill.snow.css';
import { useNavigate } from 'react-router-dom';
import CreatableSelect from 'react-select/creatable';
import { toast } from 'react-toastify';
import { Button, Icon } from 'semantic-ui-react';
import { useAuth } from '../../../contexts/AuthContext';
import { usePostAction } from '../../../contexts/PostActionContext';
import { useQuill } from '../../../contexts/QuillContext';
import axiosInstance from '../../../utils/axiosInstance';
import styles from './writePost.module.css';

const WritePost = () => {
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [nickname, setNickname] = useState('');
    const [blogName, setBlogName] = useState('');
    const [provider, setProvider] = useState('');

    const { setOnSubmit } = usePostAction();

    // 전체 카테고리
    const [categoryData, setCategoryData] = useState([]);

    // "부모" 카테고리 (단일 선택)
    const [selectedParentCategory, setSelectedParentCategory] = useState(null);

    // "자식" 카테고리 (부모 선택에 따라 달라짐, 역시 단일 선택)
    const [subCategories, setSubCategories] = useState([]);
    const [selectedChildCategory, setSelectedChildCategory] = useState(null);

    const { authInfo } = useAuth();
    const quillRef = useRef(null);
    const navigate = useNavigate();
    const { modules, formats } = useQuill();

    // 에디터 내용 변경
    const handleChange = (content) => {
        setContent(content); // 기본 HTML 저장
    };

    useEffect(() => {
        if (authInfo) {
            setNickname(authInfo.nickname);
            setBlogName(authInfo.blogName);
            setProvider(authInfo.provider);
            fetchCategories(authInfo.nickname);
        }
    }, [authInfo]);

    // 서버로부터 카테고리 목록 불러오기
    const fetchCategories = async (nickname) => {
        try {
            const response = await axiosInstance.get(`/api/categories/nickname`, {
                params: { nickname },
            });
            setCategoryData(response.data.data || []);
            return response.data.data;
        } catch (error) {
            console.error('Failed to fetch categories:', error);
            return [];
        }
    };

    // 부모 카테고리 선택 시, 해당 카테고리의 자식 목록을 subCategories에 담고, 자식 선택 초기화
    const handleParentCategorySelect = (selectedOption) => {
        setSelectedParentCategory(selectedOption);

        if (selectedOption) {
            // categoryData에서 부모 카테고리 상세정보 찾기
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

    // 부모 카테고리 새로 생성
    const handleCreateParentCategory = async (inputValue) => {
        if (!authInfo) {
            toast.error("로그인 정보가 없습니다.");
            return;
        }

        const newCategoryData = {
            name: inputValue,
        };

        axiosInstance.post('/api/categories', newCategoryData)
            .then(async () => {
                // 성공 시 전체 목록 다시 fetch
                const updatedList = await fetchCategories(authInfo.nickname);

                // 생성된 카테고리를 곧바로 선택하려면,
                // updatedList에서 이름이 inputValue인 카테고리를 찾는다
                const justCreated = updatedList?.find(cat => cat.name === inputValue);
                if (justCreated) {
                    setSelectedParentCategory({
                        label: justCreated.name,
                        value: justCreated.id,
                    });
                    // 자식 목록/선택 초기화
                    setSubCategories([]);
                    setSelectedChildCategory(null);
                }
            })
            .catch(error => {
                console.error('Failed to create parent category:', error);
                toast.error("카테고리 생성 실패");
            });
    };

    // 자식 카테고리 새로 생성
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

                // 전체 목록 다시 fetch
                const updatedList = await fetchCategories(authInfo.nickname);

                // 방금 만든 자식카테고리를 찾기 위해, parentId로 부모를 찾은 뒤 children을 확인
                const parentCat = updatedList?.find(cat => cat.id === selectedParentCategory.value);
                const children = parentCat?.children || [];
                const justCreatedChild = children.find(child => child.name === inputValue);
                if (justCreatedChild) {
                    // subCategories 업데이트
                    const newChildOption = { label: justCreatedChild.name, value: justCreatedChild.id };
                    // 다시 parentCat.children 반영
                    const mappedChildren = children.map(ch => ({ label: ch.name, value: ch.id }));
                    setSubCategories(mappedChildren);

                    // 자식카테고리를 선택
                    setSelectedChildCategory(newChildOption);
                }
            })
            .catch(error => {
                console.error('Failed to create child category:', error);
                toast.error("자식 카테고리 생성 실패");
            });
    };

    // 게시글 작성
    const handleWritePost = useCallback(async () => {
        try {
            const categoryId =
                selectedChildCategory?.value ||
                selectedParentCategory?.value ||
                null;
            console.log("게시글 생성 시 전달되는 categoryId:", categoryId);
            const response = await axiosInstance.post('/api/posts', {
                title,
                content,
                categoryId,
            });
            if (response.status === 200) {
                toast.success("게시글이 등록되었습니다.");
                navigate(`/blog/${nickname}`, { state: { blogName, provider } });
            }
        } catch (error) {
            toast.error("등록 실패. 다시 시도해주세요");
        }
    }, [selectedParentCategory, selectedChildCategory, title, content]);

    useEffect(() => {
        setOnSubmit(() => handleWritePost);
        return () => setOnSubmit(null);
    }, [handleWritePost, setOnSubmit]);

    return (
        <div>
            <Form className={styles['form-container']}>
                {/* 부모 카테고리 셀렉트 + 즉석 생성 */}
                <Form.Group>
                    <CreatableSelect
                        placeholder="부모 카테고리 선택"
                        options={categoryData
                            .filter(cat => cat && cat.name)  // cat이 있고, cat.name이 존재하는 경우만
                            .map(cat => ({
                                label: cat.name,
                                value: cat.id,
                            }))}
                        value={selectedParentCategory}
                        onChange={handleParentCategorySelect}
                        onCreateOption={handleCreateParentCategory} // 새 옵션(카테고리) 생성
                        className={styles.dropdown}
                    />
                </Form.Group>

                {/* 자식 카테고리 + 즉석 생성 */}
                {selectedParentCategory && (
                    <Form.Group>
                        <CreatableSelect
                            placeholder="자식 카테고리 선택"
                            options={subCategories}
                            value={selectedChildCategory}
                            onChange={handleChildCategorySelect}
                            onCreateOption={handleCreateChildCategory} // 새 옵션(카테고리) 생성
                            className={styles.dropdown}
                        />
                    </Form.Group>
                )}

                {/* 제목 입력 */}
                <Form.Group className={styles.title}>
                    <Form.Control
                        type="text"
                        placeholder="제목을 입력하세요"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                    />
                </Form.Group>

                {/* 내용(ReactQuill) */}
                <Form.Group>
                    <ReactQuill
                        ref={quillRef}
                        theme="snow"
                        modules={modules}
                        formats={formats}
                        className={styles['quill-editor']}
                        value={content}
                        onChange={handleChange}
                    />
                </Form.Group>
            </Form>

            {/* 작성 버튼 */}
            <div className={styles['button-group']}>
                <Button type="button" icon onClick={handleWritePost}>
                    <Icon name="edit" />
                    게시글 작성
                </Button>
            </div>
        </div>
    );
};

export default WritePost;
