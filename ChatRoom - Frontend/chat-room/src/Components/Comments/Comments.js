import React, { useState, useEffect } from "react";
//import { useDispatch } from "react-redux";
import { logout } from "../Redux/slices/userSlice";
import { hadnleCreateComment, hadnleGetComments } from "../Error/ErrorMessage";
import { createComment, getComments } from "../../Services/comments.service";
import "./Comments.css";
import { List, Input, Button } from "antd";
import { SendOutlined, CloseOutlined } from "@ant-design/icons";

const Comments = ({ onClose, idroom }) => {
 // const dispatch = useDispatch();
  const [message, setMessage] = useState("");
  const [comments, setComments] = useState([]);
  useEffect(() => {
    const getAllComments = async () => {
      try {
        const response = await getComments(idroom);
        setComments(response.data);
      } catch (error) {
        hadnleGetComments(error);
       // dispatch(logout());
      }
    };
    getAllComments();
  }, [ idroom]);

  const handleCreateComment = async () => {
    try {
      await createComment(message, idroom);
    } catch (error) {
      console.log(error);
      hadnleCreateComment(error);
     // dispatch(logout());
    }
    setMessage("");
  };

  const handleInputChange = (e) => {
    setMessage(e.target.value);
  };
  function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    const day = date.getDate();
    const month = date.getMonth() + 1;
    const year = date.getFullYear();
    const hours = date.getHours();
    const minutes = date.getMinutes();

    const formattedDate = `${day}.${month}.${year}`;
    const formattedTime = `${hours}:${minutes.toString().padStart(2, "0")}`;

    return `${formattedDate} ${formattedTime}`;
  }

  return (
    <div className="container">
      <div className="header">
        <h2 style={{ textAlign: "center" }}>Comments</h2>
        <CloseOutlined className="close-icon" key={"close"} onClick={onClose} />
      </div>
      <div className="list-container">
        <List
          itemLayout="horizontal"
          dataSource={comments}
          renderItem={(comment, index) => (
            <List.Item>
              <List.Item.Meta
                title={
                  <span>{`${comment.user.username} ${formatDateTime(comment.createdAt)}`}</span>
                }
                description={comment.text}
              />
            </List.Item>
          )}
        />
      </div>
      <div className="input-container">
        <Input
          rows={4}
          value={message}
          onChange={handleInputChange}
          placeholder="Enter your comment"
        />
        <Button
          type="primary"
          icon={<SendOutlined />}
          onClick={handleCreateComment}
        >
          Send
        </Button>
      </div>
    </div>
  );
};
export default Comments;
