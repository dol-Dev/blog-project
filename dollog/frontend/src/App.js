import { BrowserRouter, Route, Routes, useLocation } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { Header } from 'semantic-ui-react';

import './App.css';

import ManagementDashboard from './components/dashboard/management/ManagementDashboard';
import Footer from './components/footer/Footer';
import Layout from './components/layout/common/Layout';
import ManagementLayout from './components/layout/management/ManagementLayout';
import { AuthProvider } from './contexts/AuthContext';
import { BlogProvider } from './contexts/BlogContext';
import ManageCategory from './pages/category/ManageCategory';
import Index from './pages/index/Index';
import CategoryPost from './pages/post/category/CategoryPost';
import DetailPost from './pages/post/detail/DetailPost';
import UpdatePost from './pages/post/update/UpdatePost';
import WritePost from './pages/post/write/WritePost';
import BlogRedirector from './routes/helpers/BlogRedirector';
import LoginAndSignUp from './pages/user/loginAndSIgnUp/LoginAndSignUp';
import FindAccount from './pages/user/find/FindAccount';

function App() {
  return (
    <BrowserRouter>
      <BlogProvider>
        <AuthProvider>
          <AppRoutes />
          <ToastContainer
            position="top-right"
            autoClose={5000}
            hideProgressBar
            newestOnTop
            closeOnClick
            pauseOnHover
            draggable
          />
        </AuthProvider>
      </BlogProvider>
    </BrowserRouter>
  );
}

function AppRoutes() {
  const location = useLocation();
  const isLoginPage = location.pathname === '/login';
  const isFindPage = location.pathname === '/find';

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

  return (
    <Layout>
      <Routes>
        <Route path="/" exact element={<Index />} />
        <Route path="/blog/:nickname" element={<BlogRedirector />} />
        <Route path="/posts/:categoryId?" element={<CategoryPost />} />
        <Route path="/write" element={<WritePost />} />
        <Route path="/detail-post/:id" element={<DetailPost />} />
        <Route path="/update-post/:id" element={<UpdatePost />} />
        <Route path="/manage" element={<ManagementLayout />}>
          <Route index element={<ManagementDashboard />} />
          <Route path="categorys" element={<ManageCategory />} />
        </Route>
      </Routes>
    </Layout>
  );
}

export default App;
