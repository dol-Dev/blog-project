// WritePost.jsx
import React, { useEffect, useRef, useState } from 'react';
import { Form } from 'react-bootstrap';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import { useNavigate } from 'react-router-dom';
import CreatableSelect from 'react-select/creatable';
import { toast } from 'react-toastify';
import { Button, Icon } from 'semantic-ui-react';
import { useAuth } from '../../../contexts/AuthContext';
import { useQuill } from '../../../contexts/QuillContext';
import axiosInstance from '../../../utils/axiosInstance';
import styles from './writePost.module.css';

const WritePost = () => {
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [nickname, setNickname] = useState('');
    const [blogName, setBlogName] = useState('');
    const [provider, setProvider] = useState('');
    const [categoryData, setCategoryData] = useState([]);
    // 부모 카테고리는 단일 선택
    const [selectedParentCategory, setSelectedParentCategory] = useState(null);
    // 자식 카테고리도 단일 선택
    const [selectedChildCategory, setSelectedChildCategory] = useState(null);
    const [subCategories, setSubCategories] = useState([]);
    const { authInfo } = useAuth();
    const quillRef = useRef(null);
    const navigate = useNavigate();
    const { setQuillInstance, modules, formats } = useQuill();

    // 에디터 내용 변경 시 content 상태 업데이트
    const handleChange = (content) => {
        setContent(content);
    };

    // ReactQuill 마운트 후 에디터 인스턴스 등록
    useEffect(() => {
        if (quillRef.current) {
            const editor = quillRef.current.getEditor();
            setQuillInstance(editor);
        }
    }, [quillRef, setQuillInstance]);

    useEffect(() => {
        if (authInfo) {
            setNickname(authInfo.nickname);
            setBlogName(authInfo.blogName);
            setProvider(authInfo.provider);
            fetchCategories(authInfo.nickname);
        }
    }, [authInfo]);

    const fetchCategories = async (nickname) => {
        console.log(nickname)
        try {
            const response = await axiosInstance.get(`api/categories/nickname`, {
                params: { nickname }
            });
            setCategoryData(response.data.data);
        } catch (error) {
            console.error('Failed to fetch categories:', error);
        }
    };

    // 부모 카테고리 선택 시 처리
    const handleParentCategorySelect = (selectedOption) => {
        setSelectedParentCategory(selectedOption);
        // 선택한 부모 카테고리에 해당하는 자식 카테고리 추출
        if (selectedOption) {
            const parentCat = categoryData.find(cat => cat.id === selectedOption.value);
            const children = parentCat?.children || [];
            const mappedChildren = children.map(child => ({
                label: child.name,
                value: child.id,
            }));
            setSubCategories(mappedChildren);
            // 부모 변경 시 기존 자식 선택은 초기화
            setSelectedChildCategory(null);
        } else {
            setSubCategories([]);
            setSelectedChildCategory(null);
        }
    };

    // 자식 카테고리 선택 시 처리
    const handleChildCategorySelect = (selectedOption) => {
        setSelectedChildCategory(selectedOption);
    };

    const handleWritePost = async () => {
        try {
            // 자식 카테고리 선택이 있다면 우선 사용, 없으면 부모 카테고리 사용
            const categoryId = selectedChildCategory
                ? selectedChildCategory.value
                : selectedParentCategory
                    ? selectedParentCategory.value
                    : null;
            const response = await axiosInstance.post('/api/posts', {
                title,
                content,
                categoryId,
            });

            if (response.status === 200) {
                navigate(`/blog/${nickname}`, { state: { blogName, provider } });
            }
        } catch (error) {
            toast.error("등록 실패. 다시 시도해주세요");
        }
    };

    return (
        <Form className={styles['form-container']}>
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
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                />
            </Form.Group>

            {/* 내용 입력 */}
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

            {/* 작성 버튼 */}
            <div className={styles['button-group']}>
                <Button type="button" icon onClick={handleWritePost}>
                    <Icon name="edit" />
                    게시글 작성
                </Button>
            </div>
        </Form>
    );
};

export default WritePost;
