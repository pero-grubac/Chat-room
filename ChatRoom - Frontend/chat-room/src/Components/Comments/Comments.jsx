import { useState, useEffect } from "react";
import { hadnleCreateComment, hadnleGetComments } from "../Error/ErrorMessage";
import { createComment, getComments } from "../../Services/comments.service";
import "./Comments.css";
import { formatDateTime } from "../Util/formatDateTime";
import { useSessionGuard } from "../Util/useSessionGuard";
import { COMMENT_MAX_LENGTH } from "../Util/limits";
import { List, Input, Button, message as toast } from "antd";
import { SendOutlined, CloseOutlined } from "@ant-design/icons";

// Forum part: the latest 20 published comments of a room, and a box to write a new one
const Comments = ({ onClose, idroom }) => {
  const guard = useSessionGuard();
  const [message, setMessage] = useState("");
  const [comments, setComments] = useState([]);
  const [sending, setSending] = useState(false);

  useEffect(() => {
    getComments(idroom)
      .then((response) => setComments(response.data))
      .catch((error) => {
        hadnleGetComments(error);
        guard(error);
      });
  }, [idroom, guard]);

  const handleCreateComment = async () => {
    if (!message.trim()) return;
    setSending(true);
    try {
      await createComment(message.trim(), idroom);
      setMessage("");
      // New comments are hidden until a moderator publishes them
      toast.success("Sent. Your comment appears once a moderator publishes it.");
    } catch (error) {
      hadnleCreateComment(error);
      guard(error);
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="container">
      <div className="header">
        <h2 style={{ textAlign: "center" }}>Comments</h2>
        <CloseOutlined className="close-icon" aria-label="Close" onClick={onClose} />
      </div>
      <div className="list-container">
        <List
          itemLayout="horizontal"
          dataSource={comments}
          locale={{ emptyText: "No comments yet" }}
          renderItem={(comment) => (
            <List.Item>
              <List.Item.Meta
                title={<span>{`${comment.user.username} ${formatDateTime(comment.createdAt)}`}</span>}
                description={comment.text}
              />
            </List.Item>
          )}
        />
      </div>
      <div className="input-container">
        <Input
          value={message}
          maxLength={COMMENT_MAX_LENGTH}
          showCount
          onChange={(e) => setMessage(e.target.value)}
          onPressEnter={handleCreateComment}
          placeholder="Enter your comment"
        />
        <Button
          type="primary"
          icon={<SendOutlined />}
          onClick={handleCreateComment}
          loading={sending}
          disabled={!message.trim()}
        >
          Send
        </Button>
      </div>
    </div>
  );
};
export default Comments;
