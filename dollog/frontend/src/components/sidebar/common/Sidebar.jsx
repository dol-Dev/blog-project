import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import 'semantic-ui-css/semantic.min.css';
import { Icon } from 'semantic-ui-react';

import styles from './sidebar.module.css';
import axiosInstance from '../../../utils/axiosInstance';

const Sidebar = ({ userId, visible, onClose }) => {

    const [categories, setCategories] = useState([]);
    const [totalPostsCount, setTotalPostsCount] = useState(0);
    const [loading, setLoading] = useState(true);
    const [selectedCategoryId, setSelectedCategoryId] = useState('all');
    const [expandedCategories, setExpandedCategories] = useState({});
    const sidebarRef = useRef(null);
    const navigate = useNavigate();

    useEffect(() => {
        if (userId) {
            const fetchCategories = async () => {
                setLoading(true);
                try {
                    const response = await axiosInstance.get(`/api/categories/${userId}/all`);
                    console.log("response : ", JSON.stringify(response.data.data, null, 2));
    
                    const fetchedCategories = response.data.data;
    
                    // totalPostsCount 및 카테고리 변환
                    const transformedCategories = fetchedCategories.flatMap(cat => {
                        // 자식 카테고리 변환
                        const childCategories = cat.children.map(child => ({
                            id: child.id,
                            name: child.name,
                            parentId: cat.id, // 부모 ID 설정
                            postCount: child.postCount || 0, // 자식 카테고리의 포스트 수
                            childrenId: [], // 자식의 자식 ID 목록은 없음
                        }));
    
                        // 부모 카테고리
                        const parentCategory = {
                            id: cat.id,
                            name: cat.name,
                            parentId: null, // 최상위 카테고리
                            postCount: cat.postCount || 0, // 부모 자체의 포스트 수
                            childrenId: cat.children.map(child => child.id), // 자식 카테고리 ID 목록
                        };
    
                        return [parentCategory, ...childCategories];
                    });
    
                    // 전체 포스트 수 계산 (중복 방지)
                    const totalCount = fetchedCategories.reduce((acc, cat) => {
                        const parentPostCount = cat.postCount || 0; // 부모의 포스트 수
                        const childPostCount = cat.children.reduce((childAcc, child) => childAcc + (child.postCount || 0), 0); // 자식의 포스트 수
                        return acc + parentPostCount + childPostCount; // 전체 합산
                    }, 0);
    
                    setTotalPostsCount(totalCount);
    
                    // 전체보기 카테고리 추가
                    setCategories([{
                        id: 'all',
                        name: '전체보기',
                        parentId: null,
                        postCount: totalCount,
                        childrenId: [],
                    }, ...transformedCategories]);
                } catch (error) {
                    console.error('Failed to fetch categories:', error);
                }
                setLoading(false);
            };
            fetchCategories();
        }
    }, [userId]);


    useEffect(() => {
        const handleClickOutside = (event) => {
            if (sidebarRef.current && !sidebarRef.current.contains(event.target)) {
                onClose();
            }
        };

        if (visible) {
            document.addEventListener('mousedown', handleClickOutside);
        } else {
            document.removeEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [visible, onClose]);

    const toggleCategory = (id) => {
        setExpandedCategories(prev => ({
            ...prev,
            [id]: !prev[id],
        }));
    };

    const handleCategoryClick = (id) => {
        setSelectedCategoryId(id);
        toggleCategory(id);
        navigate(`/posts/${id}`);
    };

    const renderCategories = (categories) => {

        if (loading) {
            return <div className={styles['sidebar-loading']}>Loading...</div>;
        }

        return (
            <div className={styles.menu}>
                <div
                    className={`${styles['menu-item']} ${selectedCategoryId === 'all' ? styles['menu-item-active'] : ''}`}
                    onClick={() => handleCategoryClick('all')} // 전체보기일때 categoryId = all
                >
                    전체보기 ({totalPostsCount})
                </div>
                {categories.filter(cat => cat.id !== 'all' && cat.parentId === null).map(category => (
                    <React.Fragment key={category.id}>
                        <div
                            className={`${styles['menu-item']} ${selectedCategoryId === category.id ? styles['menu-item-active'] : ''}`}
                            onClick={() => { toggleCategory(category.id); setSelectedCategoryId(category.id); }}
                        >
                            <span className={styles['menu-link']}>
                                <Icon name={expandedCategories[category.id] ? 'angle down' : 'angle right'} />
                                {category.name}
                            </span>
                        </div>
                        {category.childrenId && category.childrenId.length > 0 && expandedCategories[category.id] && (
                            <div className={styles['menu-submenu']}>
                                {category.childrenId.map(childId => {
                                    const childCategory = categories.find(cat => cat.id === childId);
                                    return (
                                        <div
                                            key={childId}
                                            className={`${styles['menu-item']} ${selectedCategoryId === childId ? styles['menu-item-active'] : ''}`}
                                            onClick={() => handleCategoryClick(childId)} // 해당 자식 카테고리를 누를때 categoryId = childId
                                        >
                                            - {childCategory.name} ({childCategory.postCount})
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </React.Fragment>
                ))}
            </div>
        );
    };

    return (
        <div ref={sidebarRef} className={`${styles.sidebar} ${visible ? styles.visible : ''}`}>
            {renderCategories(categories)}
        </div>
    );
};

export default Sidebar;