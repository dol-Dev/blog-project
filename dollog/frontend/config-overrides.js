const { resolve } = require('path');

module.exports = function override(config) {
    // Webpack 설정 추가 또는 수정
    config.resolve.alias = {
        ...config.resolve.alias,
        components: resolve(__dirname, 'src/components'),
        layouts: resolve(__dirname, 'src/layouts'),
        contexts: resolve(__dirname, 'src/contexts'),
        pages: resolve(__dirname, 'src/pages'),
        utils: resolve(__dirname, 'src/utils')
    };
    return config;
};
