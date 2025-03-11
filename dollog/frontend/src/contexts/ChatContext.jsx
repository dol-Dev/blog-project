import { createContext, useContext, useState } from "react";

export const ChatContext = createContext();

export const ChatProvider = ({ children }) => {
    const [chatRoomInfo, setChatRoomInfo] = useState({ roomId: null, roomName: "" });
    const [chatVisible, setChatVisible] = useState(false);
    const [buttonVisible, setButtonVisible] = useState(true);

    return (
        <ChatContext.Provider value={{
            chatRoomInfo, setChatRoomInfo,
            chatVisible, setChatVisible,
            buttonVisible, setButtonVisible
        }}>
            {children}
        </ChatContext.Provider>
    );
};

export const useChat = () => useContext(ChatContext);
