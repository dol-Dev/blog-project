import { useEffect, useRef } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { useBlog } from '../../contexts/BlogContext';

function BlogRedirector() {
    const navigate = useNavigate();
    const location = useLocation();

    const { nickname } = useParams();
    const { blogName, provider } = location.state || {};
    const { setBlogName, setNickname, setProvider } = useBlog();

    useEffect(() => {
        if (blogName) {
            setBlogName(blogName);
        }
        if (nickname) {
            setNickname(nickname);
        }
        if (provider) {
            setProvider(provider);
        }
        navigate('/posts');
    }, [nickname, setNickname, navigate]);

    return null;
}

export default BlogRedirector;
