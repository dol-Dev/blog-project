import { useEffect } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { useBlog } from '../../contexts/BlogContext';

function BlogRedirector() {
    const navigate = useNavigate();
    const location = useLocation();

    const { nickname } = useParams();
    const { blogName } = location.state || {};
    const { setBlogName, setNickname } = useBlog();

    useEffect(() => {
        if (blogName) {
            setBlogName(blogName);
        }
        if (nickname) {
            setNickname(nickname);
        }
        navigate('/posts');
    }, [blogName, nickname, setBlogName, setNickname, navigate]);

    return null;
}

export default BlogRedirector;
