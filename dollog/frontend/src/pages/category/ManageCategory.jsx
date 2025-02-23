import React, { useEffect, useState } from 'react';
import Draggable from 'react-draggable';
import { Container, Icon, Pagination } from 'semantic-ui-react';
import Swal from "sweetalert2";
import { useAuth } from "../../contexts/AuthContext";
import axiosInstance from "../../utils/axiosInstance";
import styles from './manageCategory.module.css';

const ManageCategory = () => {
    const [categoryData, setCategoryData] = useState([]);
    const [newCategoryName, setNewCategoryName] = useState('');
    const [userId, setUserId] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalTitle, setModalTitle] = useState('');
    const [editCategoryId, setEditCategoryId] = useState(null);
    const [parentCategoryId, setParentCategoryId] = useState(null);

    const [totalPages, setTotalPages] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const { authInfo } = useAuth();

    useEffect(() => {
        fetchCategories(currentPage);
    }, [currentPage, authInfo]);

    useEffect(() => {
        if (authInfo) {
            const { id } = authInfo;
            setUserId(id);
            console.log("userId:", userId);
        }
    }, [authInfo]);

    const fetchCategories = async (page) => {
        console.log("fetchCategories - page:", page);  // 추가된 로그

        try {
            const response = await axiosInstance.get(`/api/categories/${userId}?page=${page}`);

            console.log("Fetched categories:", response.data.data.content);

            const transformedCategories = response.data.data.content.map(category => ({
                ...category,
                children: category.children || []
            }));

            setCategoryData(transformedCategories);
            setTotalPages(response.data.data.totalPages);
            setCurrentPage(page);
        } catch (error) {
            console.error("API 호출 오류:", error);
        }
    };


    const handleDelete = (id) => {
        Swal.fire({
            title: '정말 삭제하시겠습니까?',
            text: "하위 카테고리와 해당 게시물이 삭제됩니다",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: '삭제',
        }).then((result) => {
            if (result.isConfirmed) {
                axiosInstance.delete(`/api/categories/${id}`)
                    .then(() => {
                        fetchCategories(currentPage);  // currentPage를 파라미터로 전달
                    })
                    .catch(error => {
                        console.error('Failed to delete category:', error);
                    });
            }
        });
    };

    // 재귀 함수로 카테고리 트리 내에서 특정 id를 가진 카테고리를 찾음
    const findCategoryById = (categories, id) => {
        for (let category of categories) {
            if (category.id === id) {
                return category;
            }
            if (category.children && category.children.length > 0) {
                const found = findCategoryById(category.children, id);
                if (found) return found;
            }
        }
        return undefined;
    };

    const handleUpdate = (id) => {
        console.log("id:", id);
        // 최상위 배열과 그 자식들까지 포함해서 재귀적으로 검색
        const category = findCategoryById(categoryData, id);
        if (!category) {
            console.error(`Category with id ${id} not found`);
            return;
        }
        setEditCategoryId(id);
        setNewCategoryName(category.name);
        setIsModalOpen(true);
        setModalTitle('카테고리 수정하기');
    };

    const handleNewCategoryChange = (e) => {
        setNewCategoryName(e.target.value);
    };

    const openAddCategoryModal = (parentId = null) => {
        setIsModalOpen(true);
        setModalTitle(parentId ? '자식 카테고리 추가' : '새 카테고리 추가');
        setNewCategoryName('');
        setEditCategoryId(null);
        setParentCategoryId(parentId);
    };

    const handleModalClose = () => {
        setIsModalOpen(false);
        setNewCategoryName('');
        setEditCategoryId(null);
        setParentCategoryId(null);
    };

    const handleCategorySubmit = () => {
        if (!newCategoryName.trim()) {
            Swal.fire('카테고리 이름을 입력하세요', '', 'warning');
            return;
        }

        const categoryData = {
            name: newCategoryName,
            userId: userId,
            parentId: parentCategoryId,
        };

        if (authInfo) {
            if (editCategoryId) {
                axiosInstance.put(`/api/categories/${editCategoryId}`, categoryData)
                    .then(() => {
                        Swal.fire('수정 완료!', '', 'success');
                        fetchCategories(currentPage);
                        handleModalClose();
                    })
                    .catch(error => {
                        console.error('Failed to update category:', error);
                        Swal.fire('카테고리 수정에 실패했습니다', '', 'error');
                    });
            } else {
                console.log("categoryData:", categoryData);
                axiosInstance.post('/api/categories', categoryData)
                    .then(() => {
                        Swal.fire('카테고리가 추가되었습니다', '', 'success');
                        fetchCategories(currentPage);
                        handleModalClose();
                    })
                    .catch(error => {
                        console.error('Failed to add category:', error);
                        Swal.fire('카테고리 추가에 실패했습니다', '', 'error');
                    });
            }
        }
    };

    const onStop = (e, data) => {
        const movedCategoryId = parseInt(data.node.dataset.id, 10);
        const movedCategoryIndex = categoryData.findIndex(cat => cat.id === movedCategoryId);

        const newIndex = Math.round(data.y / 50);
        const reorderedCategories = [...categoryData];
        const [removed] = reorderedCategories.splice(movedCategoryIndex, 1);
        reorderedCategories.splice(newIndex, 0, removed);

        setCategoryData(reorderedCategories);

        try {
            const categoryIds = reorderedCategories.map(category => category.id);
            axiosInstance.put('/api/categories/order', categoryIds);
        } catch (error) {
            console.error('Error updating category order:', error);
        }
    };

    const handlePageChange = (e, { activePage }) => {
        setCurrentPage(activePage - 1);
    };

    return (
        <div className={styles.container}>
            <h2 className={styles.title}>카테고리 관리</h2>
            <button className={styles.addButton} onClick={() => openAddCategoryModal()}>새 카테고리 추가</button>
            <ul className={styles.categoryList}>
                {categoryData.map((category, index) => {
                    return (
                        <Draggable
                            key={category.id}
                            axis="y"
                            onStop={onStop}
                            position={{ x: 0, y: index * 50 }}  // orderIndex에 맞춰 위치 조정
                        >
                            <li data-id={category.id} className={styles.categoryItem}>
                                <div className={styles.categoryContent}>
                                    <span>{category.name}</span>
                                    <div className={styles.actions}>
                                        <button className={styles.actionButton} onClick={() => openAddCategoryModal(category.id)}>자식 추가</button>
                                        <button className={styles.actionButton} onClick={() => handleUpdate(category.id)}>수정</button>
                                        <button className={styles.actionButton} onClick={() => handleDelete(category.id)}>삭제</button>
                                    </div>
                                </div>

                                {/* 자식 카테고리 렌더링 */}
                                {category.children && category.children.length > 0 && (
                                    <ul className={styles.childCategoryList}>
                                        {category.children.map((childCategory) => {
                                            return (
                                                <li key={childCategory.id} className={styles.childCategoryItem}>
                                                    <div className={styles.categoryContent}>
                                                        <span>{childCategory.name}</span>
                                                        <div className={styles.actions}>
                                                            {/* 자식 카테고리 수정 및 삭제 */}
                                                            <button className={styles.actionButton} onClick={() => handleUpdate(childCategory.id)}>수정</button>
                                                            <button className={styles.actionButton} onClick={() => handleDelete(childCategory.id)}>삭제</button>
                                                        </div>
                                                    </div>
                                                </li>
                                            );
                                        })}
                                    </ul>
                                )}
                            </li>
                        </Draggable>
                    );
                })}
            </ul>


            {isModalOpen && (
                <div className={styles.modal}>
                    <div className={styles.modalContent}>
                        <h3>{modalTitle}</h3>
                        <input className={styles.textInput}
                            type="text"
                            value={newCategoryName}
                            onChange={handleNewCategoryChange}
                            placeholder="카테고리 이름"
                        />
                        <button className={styles.cancelButton} onClick={handleModalClose}>취소</button>
                        <button className={styles.saveButton} onClick={handleCategorySubmit}>저장</button>
                    </div>
                </div>
            )}

            <Container textAlign="center" className={styles.paginationContainer}>
                <Pagination
                    activePage={currentPage + 1}
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                    ellipsisItem={{ content: <Icon name="ellipsis horizontal" />, icon: true }}
                    firstItem={{ content: <Icon name="angle double left" />, icon: true }}
                    lastItem={{ content: <Icon name="angle double right" />, icon: true }}
                    prevItem={{ content: <Icon name="angle left" />, icon: true }}
                    nextItem={{ content: <Icon name="angle right" />, icon: true }}
                />
            </Container>
        </div>
    );
};

export default ManageCategory;
