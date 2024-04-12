import Error from "./Error";
const UnknownError = () => {
  return (
    <Error
      status="404"
      title="Something went wrong."
      subTitle=""
    />
  );
};
export default UnknownError;
