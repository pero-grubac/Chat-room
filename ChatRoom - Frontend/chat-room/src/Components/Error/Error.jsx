import { Button, Result } from "antd";
import { useNavigate } from "react-router-dom";

const Error = ({ status, title, subTitle }) => {
  const navigate = useNavigate();

  return (
    <Result
      status={status}
      title={
        <span style={{ fontWeight: "bold", color: "black", fontSize: "20px" }}>
          {title}
        </span>
      }
      subTitle={
        <span style={{ fontWeight: "bold", color: "black", fontSize: "20px" }}>
          {subTitle}
        </span>
      }
      extra={
        <Button type="primary" onClick={() => navigate("/login")}>
          Back Home
        </Button>
      }
    />
  );
};

export default Error;
