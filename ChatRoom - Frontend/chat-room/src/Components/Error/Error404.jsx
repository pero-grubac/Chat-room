import Error from "./Error";
const Error404 = () => {
  return (
    <Error
      status="404"
      title="404"
      subTitle="Sorry, the page you visited does not exist."
      />
  );
};
export default Error404;
