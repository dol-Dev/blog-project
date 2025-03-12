import React from 'react';
import { Route, Routes, useLocation } from 'react-router-dom';

import Footer from '../components/footer/Footer';
import Header from '../components/header/Header';
import Layout from '../components/layout/common/Layout';
import ManagementLayout from '../components/layout/management/ManagementLayout';

import ManagementDashboard from '../components/dashboard/management/ManagementDashboard';
import { ChatProvider } from '../contexts/ChatContext';
import ManageCategory from '../pages/category/ManageCategory';
import Index from '../pages/index/Index';
import CategoryPost from '../pages/post/category/CategoryPost';
import DetailPost from '../pages/post/detail/DetailPost';
import UpdatePost from '../pages/post/update/UpdatePost';
import WritePost from '../pages/post/write/WritePost';
import ProfileSetting from '../pages/profile/ProfileSetting';
import FindAccount from '../pages/user/find/FindAccount';
import LoginAndSignUp from '../pages/user/loginAndSIgnUp/LoginAndSignUp';
import BlogRedirector from '../routes/helpers/BlogRedirector';

import PostHeader from '../components/header/PostHeader';
import { QuillProvider } from '../contexts/QuillContext';

import ManageComment from '../pages/comment/manage/ManageComment';
import ManagePost from '../pages/post/manage/ManagePost';
import UserSetting from '../pages/user/info/UserSetting';
import ProtectedRoute from './auth/ProtectedRoute';
const AppRoutes = () => {
    const location = useLocation();
    const isLoginPage = location.pathname === '/login';
    const isFindPage = location.pathname === '/find';
    const isWriteOrUpdatePage = location.pathname === '/write' || location.pathname.startsWith('/update-post/');
    const isManagePage = location.pathname.startsWith('/manage');

    if (isLoginPage) {
        return (
            <>
                <Header />
                <Routes>
                    <Route path="/login" element={<LoginAndSignUp />} />
                </Routes>
                <Footer />
            </>
        );
    }

    if (isFindPage) {
        return (
            <>
                <Header />
                <Routes>
                    <Route path="/find" element={<FindAccount />} />
                </Routes>
                <Footer />
            </>
        );
    }

    if (isWriteOrUpdatePage) {
        return (
            <ProtectedRoute>
                <QuillProvider>
                    <PostHeader />
                    <Routes>
                        <Route path="/write" element={<WritePost />} />
                        <Route path="/update-post/:id" element={<UpdatePost />} />
                    </Routes>
                    <Footer />
                </QuillProvider>
            </ProtectedRoute>
        );
    }

    if (isManagePage) {
        return (
            <ProtectedRoute>
                <Header />
                <Routes>
                    <Route
                        path="/manage"
                        element={
                            <ProtectedRoute>
                                <ManagementLayout />
                            </ProtectedRoute>
                        }
                    >
                        <Route
                            index
                            path="dashboard"
                            element={<ManagementDashboard />
                            }
                        />
                        <Route
                            path="categorys"
                            element={
                                <ProtectedRoute>
                                    <ManageCategory />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="posts"
                            element={
                                <ProtectedRoute>
                                    <ManagePost />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="comments"
                            element={
                                <ProtectedRoute>
                                    <ManageComment />
                                </ProtectedRoute>
                            }
                        />
                    </Route>
                </Routes>
            </ProtectedRoute>
        );
    }

    return (
        <ChatProvider>
            <Layout>
                <Routes>
                    <Route path="/" element={<Index />} />
                    <Route path="/blog/:nickname" element={<BlogRedirector />} />
                    <Route path="/detail-post/:id" element={<DetailPost />} />
                    <Route
                        path="/profile"
                        element={
                            <ProtectedRoute>
                                <ProfileSetting />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/user"
                        element={
                            <ProtectedRoute>
                                <UserSetting />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/posts/:categoryId?"
                        element={
                            <ProtectedRoute>
                                <CategoryPost />
                            </ProtectedRoute>
                        }
                    />
                </Routes>
            </Layout>
        </ChatProvider>
    );
};

export default AppRoutes;
