import { useState } from 'react';
import { TwoColumnHero } from 'components';
import { usePostTravelNext } from "api/apiComponents.ts";

export const Trip = () => {
  const [content, setContent] = useState("");

  const mutation = usePostTravelNext({
    onSuccess: ({ from, to, using }) => {
      setContent("Your next trip, from: " + from + ", To: " + to + ", using: " + using);
    },
  });

  return (
    <TwoColumnHero>
      <h3 className="mb-4">Click the button</h3>

      <button onClick={() => mutation.mutate({})}>Change Content</button>

      <div>{content}</div>
    </TwoColumnHero>
  );
};
