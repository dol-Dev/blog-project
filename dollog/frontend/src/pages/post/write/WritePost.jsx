import ImageResize from 'quill-image-resize-module-react';
import React, { useEffect, useRef, useState } from 'react';
import { Form } from 'react-bootstrap';
import ReactQuill, { Quill } from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import { Navigate, useNavigate } from 'react-router-dom';
import { Button, Dropdown, DropdownItem, DropdownMenu, Icon } from 'semantic-ui-react';
import Swal from 'sweetalert2';
import { useAuth } from '../../../contexts/AuthContext';
import axiosInstance from '../../../utils/axiosInstance';
import styles from './writePost.module.css';
import { toast } from "react-toastify";

// Quill 관련 설정
Quill.register('modules/imageResize', ImageResize);
const modules = {
    toolbar: {
        container: [
            [{ header: [1, 2, 3, false] }],
            ['bold', 'italic', 'underline', 'strike'],
            ['blockquote'],
            [{ list: 'ordered' }, { list: 'bullet' }],
            [{ color: [] }, { background: [] }],
            [{ align: [] }, 'link', 'image'],
        ],
    },
    imageResize: {
        parchment: Quill.import("parchment"),
        modules: ["Resize", "DisplaySize", "Toolbar"],
    },
};

const WritePost = () => {
    const navigate = useNavigate();
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [category, setCategory] = useState(null);
    const [categoryData, setCategoryData] = useState([]);
    const { authInfo } = useAuth();
    const quillRef = useRef(null);

    useEffect(() => {
        const { id } = authInfo;
        fetchCategories(id);
    }, [authInfo]);

    if (!authInfo) {
        return <Navigate to="/" />;
    }

    const fetchCategories = async (userId) => {
        try {
            const response = await axiosInstance.get(`api/categories/${userId}/all`);
            setCategoryData(response.data.data);
        } catch (error) {
            console.error('Failed to fetch categories:', error);
        }
    };

    const processDropdownData = (data) => {
        if (!Array.isArray(data)) {
            console.error('Expected data to be an array, but got:', data);
            return [];
        }

        // 부모 카테고리의 자식 카테고리 찾기
        const findChildren = (parentId, categories = []) =>
            categories
                .filter((child) => child.parentId === parentId)
                .map((child) => {
                    if (child && child.name) {
                        // 자식의 부모 ID를 설정
                        child.parentId = parentId;
                        return {
                            key: child.id,
                            text: child.name,
                            value: child.id,
                            parentId: child.parentId,
                        };
                    } else {
                        return null;
                    }
                })
                .filter((child) => child !== null);

        return data.map((category) => {
            // 부모 카테고리의 자식 카테고리에 부모 ID 설정
            if (Array.isArray(category.children)) {
                category.children.forEach((child) => {
                    child.parentId = category.id;
                });
            }

            return {
                key: category.id,
                title: category.name,
                content: {
                    key: category.id,
                    content: findChildren(category.id, category.children || []),
                },
            };
        });
    };

    const dropdownOptions = processDropdownData(categoryData);

    const handleCategoryChange = (child) => {

        // 자식 카테고리를 찾기
        const foundCategory = categoryData
            .flatMap((cat) => cat.children)
            .find((childCategory) => childCategory.id === child.value);

        setCategory(foundCategory);  // 자식 카테고리 업데이트
    };

    // 게시글 작성
    const handleWritePost = async () => {
        try {
            const response = await axiosInstance.post('/api/posts', {
                title: title,
                content: content,
                categoryId: category.id
            });

            if (response.status === 200) {
                navigate('/');
            }
        } catch (error) {
            toast.error("수정 실패. 다시 시도해주세요.");
        }
    };

    return (
        <>
            <br /><br />
            <div className={styles.container}>
                <Form>
                    <Form.Group>
                        <Dropdown
                            placeholder="Select a category"
                            fluid
                            pointing
                            className={`item ${styles.dropdown}`}
                            text={category?.name || 'Select a category'}
                        >
                            <DropdownMenu>
                                {dropdownOptions.map((cat) => (
                                    <DropdownItem key={cat.key}>
                                        <Dropdown text={cat.title} pointing="left" className="link item">
                                            <DropdownMenu>
                                                {cat.content.content &&
                                                    cat.content.content.map((child) => (
                                                        <DropdownItem
                                                            key={child.key}
                                                            onClick={() => handleCategoryChange(child)}>
                                                            {child.text}
                                                        </DropdownItem>
                                                    ))}
                                            </DropdownMenu>
                                        </Dropdown>
                                    </DropdownItem>
                                ))}
                            </DropdownMenu>
                        </Dropdown>
                    </Form.Group>
                    <br />
                    <Form.Group>
                        <Form.Control
                            type="text"
                            placeholder="Enter title"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                        />
                    </Form.Group>
                    <br />
                    <Form.Group>
                        <ReactQuill
                            ref={quillRef}
                            theme="snow"
                            modules={modules}
                            className={styles['quill-editor']}
                            placeholder="Write your content here..."
                            value={content}
                            onChange={(val) => setContent(val)}
                        />
                    </Form.Group>
                    <br /><br /><br /><br />
                    <div className={styles['button-group']}>
                        <Button icon onClick={() => navigate('/')}>
                            <Icon name="arrow left" />
                        </Button>
                        <Button icon
                            type="button"
                            onClick={handleWritePost}>
                            <Icon name="edit" />
                        </Button>
                    </div>
                    <br /><br />
                </Form>
            </div>
        </>
    );
};

export default WritePost;