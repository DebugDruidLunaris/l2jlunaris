package jts.commons.lang.reference;

public interface HardReference<T>
{
	/** Получить объект, который удерживается **/
	public T get();

	/** Очистить сылку на удерживаемый объект **/
	public void clear();
}