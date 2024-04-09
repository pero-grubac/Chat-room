import React, { useEffect } from "react";
import { Button, Result } from "antd";
import { useNavigate } from "react-router-dom";

const Error = ({ status, title, subTitle }) => {
  const navigate = useNavigate();

  useEffect(() => {
    localStorage.clear();
  }, []);
  const redirect = () => {
    localStorage.clear();
    navigate("/login");
  };

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
        <Button type="primary" onClick={redirect}>
          Back Home
        </Button>
      }
    />
  );
};

export default Error;
